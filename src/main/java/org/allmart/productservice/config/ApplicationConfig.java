package org.allmart.productservice.config;

import org.allmart.productservice.application.port.iin.ProductUseCase;
import org.allmart.productservice.application.port.out.ProductPersistencePort;
import org.allmart.productservice.application.service.ProductService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class ApplicationConfig {

    private final ProductPersistencePort productPersistencePort;


    public ApplicationConfig(ProductPersistencePort productPersistencePort) {
        this.productPersistencePort = productPersistencePort;
    }

    @Bean
    public ProductUseCase productUseCase() {
        return new ProductService(productPersistencePort);
    }
}
