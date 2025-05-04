package org.allmart.productservice.presentation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDTO {
    @NotBlank
    public String productId;
    @NotBlank
    public String productName;
    @Min(1)
    public long stock;
    @DecimalMin(value = "0.0", inclusive = false)
    public BigDecimal unitPrice;
}