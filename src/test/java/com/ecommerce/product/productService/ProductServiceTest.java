package com.ecommerce.product.productService;

import com.ecommerce.member.memberRepository.UserRepository;
import com.ecommerce.product.productRepository.ProductCategoryRepository;
import com.ecommerce.product.productRepository.ProductImageRepository;
import com.ecommerce.product.productRepository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest
class ProductServiceTest {


    @Autowired private ProductService productService;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductCategoryRepository categoryRepository;
    @Mock
    private ProductImageRepository imageRepository;
    @Mock
    private UserRepository userRepository;


    @Test
    void isBeanIsNotNull() {
        then(productService).isNotNull();
    }


}
