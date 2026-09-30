package com.arajit.service;

import com.arajit.model.ProductInfo;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StockChecker {

    private Browser browser;
    private Playwright playwright;

    public StockChecker() {
        initializePlaywright();
    }

    private void initializePlaywright() {
        try {
            playwright = Playwright.create();
            browser = playwright.chromium().launch();
            log.info("✅ Playwright browser initialized");
        } catch (Exception e) {
            log.error("❌ Failed to initialize Playwright: {}", e.getMessage(), e);
            throw new RuntimeException("Playwright initialization failed", e);
        }
    }

    public List<ProductInfo> checkStock(String url) {
        if (url.contains("rooter.gg")) {
            return checkRooter(url);
        } else if (url.contains("jiogames.com")) {
            return checkJioGames(url);
        } else if (url.contains("flipkart.com")) {
            return checkFlipkart(url);
        } else if (url.contains("gamestheshop.com")) {
            return checkGamesTheShop(url);
        } else {
            log.warn("⚠️ Unknown site, using generic checker for: {}", url);
            return checkGeneric(url);
        }
    }

    // ─────────────────────────────────────────────
    // ROOTER: Single product page with denomination
    // cards (Rs.1000, Rs.2000 etc.)
    // ─────────────────────────────────────────────
    private List<ProductInfo> checkRooter(String url) {
        List<ProductInfo> inStockProducts = new ArrayList<>();

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            log.debug("📄 Rooter page loaded");

            // Check main button first — if Out of Stock, nothing to check
            try {
                ElementHandle mainBtn = page.querySelector("button[data-testid='proceed-to-pay']");
                if (mainBtn != null) {
                    String btnText = mainBtn.textContent().trim();
                    String disabled = mainBtn.getAttribute("data-disabled");
                    log.debug("🔘 Rooter main button: '{}' | disabled: {}", btnText, disabled);

                    if (btnText.equalsIgnoreCase("Out of Stock") || "true".equals(disabled)) {
                        log.info("❌ Rooter: All denominations Out of Stock");
                        return inStockProducts;
                    }
                }
            } catch (PlaywrightException e) {
                log.debug("Could not find Rooter main button");
            }

            // Check individual denomination cards
            List<ElementHandle> denomCards = page.querySelectorAll("[class*='variationContainer'] > *, [class*='variation'] > *");

            if (denomCards.isEmpty()) {
                denomCards = page.querySelectorAll("[class*='wrapper'] [class*='root']");
            }

            log.info("🔍 Rooter: Found {} denomination cards", denomCards.size());

            for (ElementHandle card : denomCards) {
                try {
                    String cardText = card.textContent().trim();

                    if (cardText.toLowerCase().contains("out of stock")) {
                        log.debug("❌ Out of Stock: {}", cardText.replaceAll("\\s+", " "));
                        continue;
                    }

                    if (cardText.contains("Rs.") || cardText.contains("₹")) {
                        String denomination = cardText.replaceAll("\\s+", " ").trim();
                        ProductInfo product = new ProductInfo();
                        product.setName("PlayStation Gift Card - " + denomination);
                        product.setPrice(denomination);
                        product.setUrl(url);
                        product.setStockIndicator("Denomination card active");
                        product.setDetectedAt(System.currentTimeMillis());
                        inStockProducts.add(product);
                        log.info("✅ IN STOCK: {}", denomination);
                    }
                } catch (Exception e) {
                    log.debug("Error reading denomination card: {}", e.getMessage());
                }
            }

            // Fallback if no denomination cards found but main button is active
            if (inStockProducts.isEmpty()) {
                try {
                    ElementHandle mainBtn = page.querySelector("button[data-testid='proceed-to-pay']");
                    if (mainBtn != null) {
                        String btnText = mainBtn.textContent().trim();
                        if (!btnText.equalsIgnoreCase("Out of Stock")) {
                            ProductInfo product = new ProductInfo();
                            product.setName("PlayStation Gift Card India");
                            product.setPrice("Check site for price");
                            product.setUrl(url);
                            product.setStockIndicator("Main button active: " + btnText);
                            product.setDetectedAt(System.currentTimeMillis());
                            inStockProducts.add(product);
                            log.info("✅ Rooter IN STOCK (fallback): {}", btnText);
                        }
                    }
                } catch (PlaywrightException e) {
                    log.debug("Rooter fallback check failed");
                }
            }

        } catch (Exception e) {
            log.error("❌ Error checking Rooter: {}", e.getMessage(), e);
        }

        return inStockProducts;
    }

    // ─────────────────────────────────────────────
    // JIOGAMES: Product grid with multiple cards
    // ─────────────────────────────────────────────
    private List<ProductInfo> checkJioGames(String url) {
        List<ProductInfo> inStockProducts = new ArrayList<>();

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            log.debug("📄 JioGames page loaded");

            List<ElementHandle> productCards = page.querySelectorAll(".product-card, [data-product], .product-item");

            if (productCards.isEmpty()) {
                productCards = page.querySelectorAll("div[class*='product'], article[class*='product']");
            }

            log.info("🔍 JioGames: Found {} product cards", productCards.size());

            for (ElementHandle card : productCards) {
                try {
                    String productName = null;
                    ElementHandle nameEl = card.querySelector("h2, h3, [class*='name'], .title");
                    if (nameEl != null) productName = nameEl.textContent().trim();

                    String productPrice = null;
                    ElementHandle priceEl = card.querySelector("[class*='price'], .amount, .cost");
                    if (priceEl != null) productPrice = priceEl.textContent().trim();

                    String productUrl = url;
                    ElementHandle linkEl = card.querySelector("a[href]");
                    if (linkEl != null) {
                        String href = linkEl.getAttribute("href");
                        productUrl = (href != null && href.startsWith("http")) ? href : url + href;
                    }

                    boolean inStock = false;
                    String inStockIndicator = "";

                    ElementHandle addBtn = card.querySelector("button[class*='add'], button[class*='cart']");
                    if (addBtn != null && !"true".equals(addBtn.getAttribute("disabled"))) {
                        inStock = true;
                        inStockIndicator = "Add to Cart active";
                    }

                    ElementHandle buyBtn = card.querySelector("button[class*='buy']");
                    if (buyBtn != null && !"true".equals(buyBtn.getAttribute("disabled"))) {
                        inStock = true;
                        inStockIndicator = "Buy Now active";
                    }

                    // Out of stock text always overrides
                    ElementHandle stockEl = card.querySelector("[class*='stock']");
                    if (stockEl != null) {
                        String stockText = stockEl.textContent().toLowerCase();
                        if (stockText.contains("out of stock") || stockText.contains("sold out")) {
                            inStock = false;
                            inStockIndicator = "Out of Stock";
                        } else if (stockText.contains("in stock")) {
                            inStock = true;
                            inStockIndicator = "In Stock text";
                        }
                    }

                    if (inStock && productName != null && !productName.isEmpty()) {
                        ProductInfo product = new ProductInfo();
                        product.setName(productName);
                        product.setPrice(productPrice != null ? productPrice : "N/A");
                        product.setUrl(productUrl);
                        product.setStockIndicator(inStockIndicator);
                        product.setDetectedAt(System.currentTimeMillis());
                        inStockProducts.add(product);
                        log.info("✅ IN STOCK: {} | {}", productName, productPrice);
                    }

                } catch (Exception e) {
                    log.debug("Error processing JioGames card: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("❌ Error checking JioGames: {}", e.getMessage(), e);
        }

        return inStockProducts;
    }

    // ─────────────────────────────────────────────
    // FLIPKART: Bot-protected, uses real user-agent
    // and text-based detection (class names change)
    // ─────────────────────────────────────────────
    private List<ProductInfo> checkFlipkart(String url) {
        List<ProductInfo> inStockProducts = new ArrayList<>();

        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .setViewportSize(1366, 768)
                        .setLocale("en-IN")
        )) {
            Page page = context.newPage();
            page.setExtraHTTPHeaders(Map.of(
                    "Accept-Language", "en-IN,en;q=0.9",
                    "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
            ));

            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForTimeout(3000);

            log.debug("📄 Flipkart page loaded");

            boolean inStock = false;
            String indicator = "";

            // Try button selector first
            try {
                ElementHandle addBtn = page.querySelector("button._2KpZ6l._2U9uOA._3v1-ww, button[class*='_2KpZ6l']");
                if (addBtn != null) {
                    String btnText = addBtn.textContent().trim();
                    log.debug("🔘 Flipkart button: '{}'", btnText);
                    if (btnText.equalsIgnoreCase("Add to Cart") || btnText.equalsIgnoreCase("Buy Now")) {
                        inStock = true;
                        indicator = "Button found: " + btnText;
                    }
                }
            } catch (PlaywrightException e) {
                log.debug("Flipkart button selector failed, falling back to text");
            }

            // Fallback: page text check
            if (!inStock) {
                String bodyText = page.textContent("body");
                boolean hasOutOfStock = bodyText.toLowerCase().contains("out of stock")
                        || bodyText.toLowerCase().contains("currently unavailable")
                        || bodyText.toLowerCase().contains("sold out");
                boolean hasBuyOption = bodyText.contains("Add to Cart")
                        || bodyText.contains("Buy Now");

                log.debug("Flipkart text — buy: {}, oos: {}", hasBuyOption, hasOutOfStock);

                if (hasBuyOption && !hasOutOfStock) {
                    inStock = true;
                    indicator = "Buy option in page text";
                }
            }

            if (inStock) {
                String productName = "PlayStation Gift Card";
                try {
                    ElementHandle titleEl = page.querySelector("span.B_NuCI, h1[class*='title'], [class*='_35KyD6']");
                    if (titleEl != null) productName = titleEl.textContent().trim();
                } catch (PlaywrightException e) {
                    log.debug("Could not get Flipkart product name");
                }

                ProductInfo product = new ProductInfo();
                product.setName(productName);
                product.setPrice("₹1000");
                product.setUrl(url);
                product.setStockIndicator(indicator);
                product.setDetectedAt(System.currentTimeMillis());
                inStockProducts.add(product);
                log.info("✅ Flipkart IN STOCK: {}", productName);
            } else {
                log.info("❌ Flipkart: Out of Stock or unavailable");
            }

        } catch (Exception e) {
            log.error("❌ Error checking Flipkart: {}", e.getMessage(), e);
        }

        return inStockProducts;
    }

    // ─────────────────────────────────────────────
    // GAMES THE SHOP: Single product page
    // Stock shown as "Currently Out of Stock" in red
    // or "Add to Cart" when available
    // ─────────────────────────────────────────────
    private List<ProductInfo> checkGamesTheShop(String url) {
        List<ProductInfo> inStockProducts = new ArrayList<>();

        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        )) {
            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForTimeout(3000);

            log.debug("📄 GamesTheShop page loaded");

            String bodyText = page.textContent("body");

            boolean outOfStock = bodyText.toLowerCase().contains("currently out of stock")
                    || bodyText.toLowerCase().contains("out of stock")
                    || bodyText.toLowerCase().contains("sold out");

            boolean hasAddToCart = bodyText.toLowerCase().contains("add to cart")
                    || bodyText.toLowerCase().contains("buy now");

            log.debug("GamesTheShop — outOfStock: {}, addToCart: {}", outOfStock, hasAddToCart);

            if (hasAddToCart && !outOfStock) {
                String productName = "PlayStation Gift Card";
                try {
                    ElementHandle titleEl = page.querySelector("h1");
                    if (titleEl != null) productName = titleEl.textContent().trim();
                } catch (PlaywrightException e) {
                    log.debug("Could not get GamesTheShop product name");
                }

                ProductInfo product = new ProductInfo();
                product.setName(productName);
                product.setPrice("₹1000");
                product.setUrl(url);
                product.setStockIndicator("Add to Cart button active");
                product.setDetectedAt(System.currentTimeMillis());
                inStockProducts.add(product);
                log.info("✅ GamesTheShop IN STOCK: {}", productName);
            } else {
                log.info("❌ GamesTheShop: Currently Out of Stock");
            }

        } catch (Exception e) {
            log.error("❌ Error checking GamesTheShop: {}", e.getMessage(), e);
        }

        return inStockProducts;
    }

    // ─────────────────────────────────────────────
    // GENERIC fallback for any other site
    // ─────────────────────────────────────────────
    private List<ProductInfo> checkGeneric(String url) {
        List<ProductInfo> inStockProducts = new ArrayList<>();

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions().setTimeout(30000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            String pageText = page.textContent("body").toLowerCase();
            boolean hasOutOfStock = pageText.contains("out of stock") || pageText.contains("sold out");
            boolean hasBuyOption = pageText.contains("add to cart") || pageText.contains("buy now") || pageText.contains("in stock");

            if (hasBuyOption && !hasOutOfStock) {
                ProductInfo product = new ProductInfo();
                product.setName("Product at " + url);
                product.setPrice("N/A");
                product.setUrl(url);
                product.setStockIndicator("Generic: buy text found, no OOS text");
                product.setDetectedAt(System.currentTimeMillis());
                inStockProducts.add(product);
                log.info("✅ Generic IN STOCK detected at: {}", url);
            }

        } catch (Exception e) {
            log.error("❌ Error in generic checker: {}", e.getMessage(), e);
        }

        return inStockProducts;
    }

    public void shutdown() {
        try {
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
            log.info("✅ Playwright resources cleaned up");
        } catch (Exception e) {
            log.error("Error shutting down Playwright: {}", e.getMessage());
        }
    }
}