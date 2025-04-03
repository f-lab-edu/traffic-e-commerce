package com.ecommerce.product.productService;

import com.ecommerce.product.dto.request.ProductModifyRequest;
import com.ecommerce.product.dto.request.ProductRegisterRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.dto.search.DetailedSearchCondition;
import com.ecommerce.product.productEntity.Product;
import com.ecommerce.product.productEntity.ProductCategory;
import com.ecommerce.product.productEntity.ProductImage;
import com.ecommerce.product.productEntity.ProductStatus;
import com.ecommerce.product.productRepository.ProductCategoryRepository;
import com.ecommerce.product.productRepository.ProductImageRepository;
import com.ecommerce.product.productRepository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ProductImageRepository productImageRepository;


    // 판매자: 전체 상품 조회
    public List<ProductResponse> getSellerProducts(Long sellerId) {
        List<Product> sellerProducts = productRepository.findAllBySellerId(sellerId);

        return sellerProducts.stream().map(ProductResponse::from).collect(Collectors.toList());
    }

    // 판매자: 상태값을 필터링해서 상품 조회
    public List<ProductResponse> getSellerProducts(Long sellerId, String status) {
        ProductStatus requestedStatus = ProductStatus.valueOf(status);
        List<Product> sellerProducts = productRepository.findBySellerIdAndStatus(sellerId, requestedStatus);

        return sellerProducts.stream().map(ProductResponse::from).collect(Collectors.toList());
    }



    // 판매자: 상품 최초 등록
    public Product registerProduct(Long sellerId, ProductRegisterRequest registerRequest) {
        ProductCategory category = categoryRepository.findByName(registerRequest.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find category"));

        Product product = Product.of(sellerId, registerRequest, category);
        Product savedProduct = productRepository.save(product);

        List<ProductImage> imageList = registerRequest.getImageUrls().stream().map(url ->
                ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(url)
                        .build()

        ).collect(Collectors.toList());

        imageRepository.saveAll(imageList);

        return Product.of(savedProduct, imageList);
    }

    // 판매자: 등록된 상품 정보 변경
    public Product modifyProduct(Long sellerId, Long productId, ProductModifyRequest modifyRequest) throws AccessDeniedException {
        Product product = productRepository.findByProductId(productId).orElseThrow(
                () -> new IllegalArgumentException("Cannot find products")
        );

        if (product.getSellerId() != (sellerId)) {
            throw new AccessDeniedException("not equal to seller");
        }

        productImageRepository.deleteByProduct(product);

        List<ProductImage> imageList = modifyRequest.getImageUrls().stream().map(url ->
                ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .build()

        ).collect(Collectors.toList());

        imageRepository.saveAll(imageList);

        Product modifiedProduct = Product.of(product, imageList);
        return productRepository.save(modifiedProduct);
    }

    // 판매자: 등록된 상품 삭제(소프트 삭제)
    public void deleteProduct(Long sellerId, Long productId) throws AccessDeniedException {

        Product product = productRepository.findByProductId(productId).orElseThrow(
                () -> new IllegalArgumentException("No products")
        );

        if (product.getId() != (sellerId)) {
            throw new AccessDeniedException("products not matched seller Id ");
        }
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
    }


    // 구매자: 아이템 조회
    public ProductResponse getProductDetail(Long productId) {
        var detailedProduct = productRepository.findByProductId(productId).filter(item -> item.getStatus() != ProductStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("There are no products or deleted"));

        return ProductResponse.from(detailedProduct);
    }

    // 구매자: 상세 아이템 조회
    public Page<ProductResponse> detailSearchProduct(DetailedSearchCondition condition) {
        Page<Product> searchedProduct = productRepository.searchProductForCustomer(
                condition.getName()
                , condition.getCategoryId()
                , condition.getMinPrice()
                , condition.getMaxPrice()
                , condition.getPageable());

        return searchedProduct.map(ProductResponse::from);

    }


}
