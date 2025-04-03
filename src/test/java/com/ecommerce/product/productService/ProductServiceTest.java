package com.ecommerce.product.productService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest
class ProductServiceTest {


    @Autowired private ProductService productService;

    @Test
    void isBeanIsNotNull() {
        then(productService).isNotNull();
    }


}
