package com.ecommerce.product.productController;

import com.ecommerce.member.memberRepository.UserRepository;
import com.ecommerce.product.dto.request.ProductModifyRequest;
import com.ecommerce.product.dto.request.ProductRegisterRequest;
import com.ecommerce.product.productEntity.Product;
import com.ecommerce.product.productEntity.ProductCategory;
import com.ecommerce.product.productRepository.ProductCategoryRepository;
import com.ecommerce.product.productRepository.ProductImageRepository;
import com.ecommerce.product.productRepository.ProductRepository;
import com.ecommerce.util.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@Transactional
class productControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductCategoryRepository categoryRepository;
    @Autowired
    private ProductImageRepository imageRepository;
    @Autowired
    private JwtService jwtService;

    @Test
    void registerProduct_success() throws Exception {
        // Given
        String sellerId = "bdd36fb4-02a9-4ef0-8596-db61cc6d6c9a";
        String token = jwtService.generateToken(sellerId);

        ProductRegisterRequest registerRequest = ProductRegisterRequest.builder()
                .name("MacBook").description("Apple M4 pro").price(BigDecimal.valueOf(999)).stockQuantity(10).category("electronics").imageUrls(List.of("image1.jpg", "image2.jpg"))
                .build();

        // When & Then
        mockMvc.perform(
                        post("/product/register")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))

                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("MacBook"));
    }

    @Test
    void modifyProduct_success() throws Exception {
        // Given
        String sellerId = "bdd36fb4-02a9-4ef0-8596-db61cc6d6c9a";
        String token = jwtService.generateToken(sellerId);

        ProductCategory category = categoryRepository.save(ProductCategory.of("electronics"));


        ProductModifyRequest modifyRequest = ProductModifyRequest.builder()
                .name("Galaxy S25").description("S25 Ultra plus").price(BigDecimal.valueOf(1000)).stockQuantity(50).category(category.getName()).imageUrls(List.of("image-galaxy-1.jpg", "image-galaxy-2.jpg", "image-galaxy-3.jpg"))
                .build();


        // When & Then
        mockMvc.perform(
                        post("/product/register")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(modifyRequest))

                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("S25 Ultra plus"));

    }

    @Test
    void getItem() throws Exception {
        // Given
        String sellerId = "bdd36fb4-02a9-4ef0-8596-db61cc6d6c9a";
        String token = jwtService.generateToken(sellerId);
        String extractUserId = jwtService.extractUserId(token);

        ProductCategory category = categoryRepository.save(ProductCategory.of("electronics"));

        Product buildProduct = Product.builder()
                .name("Iphone 24").price(BigDecimal.valueOf(10000)).sellerId(UUID.fromString(extractUserId)).stockQuantity(643).category(category).isActive(true).isDeleted(false)
                .build();

        productRepository.save(buildProduct);

        // When & Then
        mockMvc.perform(post("/product/detail" + buildProduct.getProductUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(10000));
    }

    @Test
    void getItem_queryString() throws Exception {
        // Given
        String sellerId = "bdd36fb4-02a9-4ef0-8596-db61cc6d6c9a";
        String token = jwtService.generateToken(sellerId);
        String extractUserId = jwtService.extractUserId(token);

        ProductCategory category = categoryRepository.save(ProductCategory.of("electronics"));

        Product buildProduct = Product.builder()
                .name("GalaxyBuz").price(BigDecimal.valueOf(150)).sellerId(UUID.fromString(extractUserId)).stockQuantity(4).category(category).isActive(true).isDeleted(false)
                .build();

        productRepository.save(buildProduct);

        // When & Then
        mockMvc.perform(get("/product/search").param("name", "GalaxyBuz")
                        .param("category", String.valueOf(category.getId())).param("minPrice", "100").param("maxPrice", "200")
                        .param("page", "0")         // 0부터 시작하는 페이지
                        .param("size", "20")        // 한 페이지에 20개
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("GalaxyBuz"));


    }


}
