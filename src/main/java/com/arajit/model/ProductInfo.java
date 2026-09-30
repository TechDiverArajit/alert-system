package com.arajit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInfo {
    private String name;
    private String price;
    private String url;
    private String stockIndicator;
    private long detectedAt;
}
