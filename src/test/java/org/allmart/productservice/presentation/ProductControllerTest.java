package org.allmart.productservice.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.allmart.productservice.application.port.iin.ProductUseCase;
import org.allmart.productservice.domain.Product;
import org.allmart.productservice.presentation.dto.ProductRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductUseCase productUseCase;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /product-service/products/all - 전체 상품 조회")
    void getAllProducts() throws Exception {
        // given
        Product product = new Product.Builder("test1", "테스트1상품")
                .stock(100)
                .createdAt(LocalDateTime.now())
                .unitPrice(BigDecimal.valueOf(10000))
                .build();

        when(productUseCase.getAllProducts()).thenReturn(List.of(product));

        // when & then
        mockMvc.perform(get("/product-service/products/all"))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("GET /product-service/products/{productId} - 상품 단건 조회")
    void getProductById() throws Exception {
        //given
        Product product = new Product.Builder("test1", "테스트1상품")
                .stock(100)
                .createdAt(LocalDateTime.now())
                .unitPrice(BigDecimal.valueOf(10000))
                .build();

        when(productUseCase.getProductById(product.getProductId())).thenReturn(product);

        // when & then
        mockMvc.perform(get("/product-service/products/{productId}", product.getProductId()))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("POST /product-service/products/register - 상품 등록")
    void registerProduct() throws Exception {
        //given
        ProductRequestDTO dto = ProductRequestDTO.builder()
                .productId("test1")
                .productName("테스트1상품")
                .stock(100)
                .unitPrice(BigDecimal.valueOf(10000))
                .build();

        Product product = new Product.Builder(dto.getProductId(), dto.getProductName())
                .stock(dto.getStock())
                .unitPrice(dto.getUnitPrice())
                .createdAt(LocalDateTime.now())
                .build();

        when(productUseCase.registerProduct(any())).thenReturn(product);

        // when & then
        mockMvc.perform(post("/product-service/products/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }



    @Test
    @DisplayName("POST /product-service/products/{productId}/stock/decrease - 재고 차감")
    void decreaseProductStock() throws Exception {
        //given
        String productId = "test1";
        int quantity = 10;

        Product updatedProduct = new Product.Builder(productId, "테스트1상품")
                .stock(90)
                .createdAt(LocalDateTime.now())
                .unitPrice(BigDecimal.valueOf(10000))
                .build();

        when(productUseCase.decreaseStock(productId, quantity)).thenReturn(updatedProduct);


        // when & then
        mockMvc.perform(post("/product-service/products/{productId}/stock/decrease", productId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk());

    }



    @Test
    @DisplayName("POST /product-service/products/{productId}/stock/increase - 재고 증가")
    void increaseProductStock() throws Exception {
        //given
        String productId = "test1";
        int quantity = 10;

        Product updatedProduct = new Product.Builder(productId, "테스트1상품")
                .stock(110)
                .createdAt(LocalDateTime.now())
                .unitPrice(BigDecimal.valueOf(10000))
                .build();

        when(productUseCase.increaseStock(productId, quantity)).thenReturn(updatedProduct);

        // when & then
        mockMvc.perform(post("/product-service/products/{productId}/stock/increase", productId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk());

    }
}