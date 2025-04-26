package org.allmart.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {

    private String productId;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    private String orderId;
    private String userId;
}
