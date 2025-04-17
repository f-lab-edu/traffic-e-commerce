package com.ecommerce.product.productService;

import com.ecommerce.member.memberEntity.Users;
import com.ecommerce.member.memberRepository.UserRepository;
import com.ecommerce.product.dto.request.ProductModifyRequest;
import com.ecommerce.product.dto.request.ProductRegisterRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.dto.search.DetailedSearchCondition;
import com.ecommerce.product.productEntity.Product;
import com.ecommerce.product.productEntity.ProductCategory;
import com.ecommerce.product.productEntity.ProductImage;
import com.ecommerce.product.productRepository.ProductCategoryRepository;
import com.ecommerce.product.productRepository.ProductImageRepository;
import com.ecommerce.product.productRepository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;

    // 판매자: 전체 상품 조회
    public List<ProductResponse> getSellerProducts(UUID sellerId) {
        List<Product> sellerProducts = productRepository.findAllBySellerId(sellerId);

        return sellerProducts.stream().map(ProductResponse::from).collect(Collectors.toList());
    }

    // 판매자: 상태값을 필터링해서 상품 조회
    public List<ProductResponse> getSellerProducts(UUID sellerId, boolean isActive) {
        List<Product> sellerProducts = productRepository.findBySellerIdAndIsActive(sellerId, isActive);

        return sellerProducts.stream().map(ProductResponse::from).collect(Collectors.toList());
    }


    // 판매자: 상품 최초 등록
    public Product registerProduct(String sellerId, ProductRegisterRequest registerRequest) {
        // 판매자 정보 조회
        Users seller = userRepository.findByUserId(UUID.fromString(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        // 카테고리 정보 조회
        ProductCategory category = categoryRepository.findByName(registerRequest.getCategory())
                .orElseGet(() -> categoryRepository.save(ProductCategory.of(registerRequest.getCategory())));

        Product product = Product.of(seller.userId, registerRequest, category);
        Product savedProduct = productRepository.save(product);

        List<ProductImage> imageList = registerRequest.getImageUrls().stream()
                .map(url -> ProductImage.of(savedProduct, url))
                .toList();

        imageRepository.saveAll(imageList);

        return Product.of(savedProduct, imageList);
    }

    // 판매자: 등록된 상품 정보 변경
    public Product modifyProduct(String sellerId, String productId, ProductModifyRequest modifyRequest) {
        Product product = productRepository.findByProductUUID(UUID.fromString(productId))
                .filter(p -> !p.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find products"));

        // 카테고리 정보 변경
        categoryRepository.save(ProductCategory.of(modifyRequest.getCategory()));

        // 상품 이미지 초기화
        imageRepository.deleteByProduct(product);

        List<ProductImage> imageList = modifyRequest.getImageUrls().stream()
                .map(url -> ProductImage.of(product, url))
                .toList();

        imageRepository.saveAll(imageList);

        Product modifiedProduct = Product.of(product, imageList);
        return productRepository.save(modifiedProduct);
    }

    // 판매자: 등록된 상품 삭제(소프트 삭제)
    public void deleteProduct(String sellerId, String productId) {
        // 판매자 정보 조회
        Users seller = userRepository.findByUserId(UUID.fromString(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid user"));


        Product product = productRepository.findByProductUUID(UUID.fromString(productId))
                .filter(p -> p.getSellerId().equals(seller.userId))
                .orElseThrow(() -> new IllegalArgumentException("No products"));

        // 소프트삭제
        product.softDelete();
        productRepository.save(product);
    }


    // 구매자: 아이템 조회
    public ProductResponse getProductDetail(String productId) {
        var detailedProduct = productRepository.findByProductUUID(UUID.fromString(productId))
                .filter(p -> p.isActive() && !p.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("There are no products or deleted"));

        return ProductResponse.from(detailedProduct);
    }

    // 구매자: 상세 아이템 조회
    public Page<ProductResponse> searchDetailForCustomer(DetailedSearchCondition condition) {
        Page<Product> searchedProduct = productRepository.searchProductForCustomer(
                condition.getName()
                , condition.getCategoryId()
                , condition.getMinPrice()
                , condition.getMaxPrice()
                , condition.getPageable());

        return searchedProduct.map(ProductResponse::from);

    }


}
