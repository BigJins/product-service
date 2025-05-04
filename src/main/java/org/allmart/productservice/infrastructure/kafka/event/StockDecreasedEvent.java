package org.allmart.productservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDecreasedEvent {
    private String productId;
    private int quantity;
    private String type = "StockDecreased";
}
