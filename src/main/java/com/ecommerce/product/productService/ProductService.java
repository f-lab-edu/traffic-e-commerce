package com.ecommerce.product.productService;

import com.ecommerce.member.memberEntity.Users;
import com.ecommerce.member.memberRepository.UserRepository;
import com.ecommerce.product.dto.request.ProductModifyRequest;
import com.ecommerce.product.dto.request.ProductRegisterRequest;
import com.ecommerce.product.dto.request.StockDeductRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.dto.response.StockDeductResult;
import com.ecommerce.product.dto.search.DetailedSearchCondition;
import com.ecommerce.product.event.ActiveStockEventPublisher;
import com.ecommerce.product.productEntity.Product;
import com.ecommerce.product.productEntity.ProductCategory;
import com.ecommerce.product.productEntity.ProductImage;
import com.ecommerce.product.productRepository.ProductCategoryRepository;
import com.ecommerce.product.productRepository.ProductImageRepository;
import com.ecommerce.product.productRepository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ActiveStockEventPublisher eventPublisher;

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
    @Transactional
    public Product modifyProduct(String sellerId, String productId, ProductModifyRequest modifyRequest) {
        Product product = productRepository.findByProductUUID(UUID.fromString(productId))
                .filter(p -> p.getSellerId().equals(sellerId))
                .orElseThrow(() -> new IllegalArgumentException("Cannot find products"));

        // 기존 이미지 소프트 딜리트 처리
        List<ProductImage> images = imageRepository.findByProductNotDeleted(product);
        images.forEach(ProductImage::softDelete);

        // 새 이미지들 저장
        List<ProductImage> newImages = modifyRequest.getImageUrls().stream()
                .map(url -> ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .isActive(true)      // 활성 상태
                        .isDeleted(false)    // 삭제 안된 상태
                        .build())
                .toList();

        imageRepository.saveAll(newImages);

        // 상품 이미지 초기화
        List<ProductImage> imageList = modifyRequest.getImageUrls().stream()
                .map(url -> ProductImage.of(product, url))
                .toList();

        // 카테고리 정보 변경
        categoryRepository.save(ProductCategory.of(modifyRequest.getCategory()));

        // 상품 정보 업데이트
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

        // 상품 소프트삭제
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


    // 단일 상품 재고 차감
    public StockDeductResult deductStock(UUID productUUID, int quantity, UUID orderUUID) {
        log.info("Request deduce qty: productUUID={}, qty={}, orderUUID={}", productUUID, quantity, orderUUID);

        try {
            // 비관적 락으로 상품 조회
            Product product = productRepository.findByProductUUIDWithLock(productUUID)
                    .orElseThrow(() -> new IllegalArgumentException("cannot find product: " + productUUID));

            // 재고 차감 시도
            boolean success = product.deductStock(quantity);

            if (success) {
                // 성공 시 이벤트 발행
                eventPublisher.publishStockDeduct(orderUUID, productUUID, quantity, product.getStockQuantity());
                log.info("Success deduct stock: productUUID={}, remainingQty={}", productUUID, product.getStockQuantity());

                return StockDeductResult.success(productUUID, product.getStockQuantity());
            } else {
                // 실패 시 이벤트 발행
                eventPublisher.publishStockLacked(orderUUID, productUUID, quantity, product.getStockQuantity());
                log.warn("Stock lacked: productUUID={}, requested qty={}, current qty={}", productUUID, quantity, product.getStockQuantity());

                return StockDeductResult.failed(productUUID);
            }

        } catch (Exception e) {
            log.error("Fail to deduct stock : productUUID={}, error={}", productUUID, e.getMessage());
            return StockDeductResult.failed(productUUID);
        }
    }

    // 여러 상품 재고 차감
    public void deductStocks(List<StockDeductRequest> requests, UUID orderUUID) {
        List<StockDeductResult> results = new ArrayList<>();

        List<UUID> productUUIDs = requests.stream().map(StockDeductRequest::getProductUUID)
                .sorted()
                .collect(Collectors.toList());

        List<Product> products = productRepository.findByProductUUIDsWithLock(productUUIDs);
        Map<UUID, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductUUID, Function.identity()));

        for(StockDeductRequest request : requests) {
            Product product = productMap.get(request.getProductUUID());

            boolean deductSucceed = product.deductStock(request.getQuantity());


            if (deductSucceed) {
                results.add(StockDeductResult.success(request.getProductUUID(), product.getStockQuantity()));
            } else {
                results.add(StockDeductResult.failed(request.getProductUUID()));
            }

        }

        boolean hasAllsuccess = results.stream().allMatch(StockDeductResult::isSuccess);

        if (hasAllsuccess) {
            eventPublisher.publishBulkStockDeduct(orderUUID, results);
        } else {
            eventPublisher.publishBulkStockFailed(orderUUID, results);
        }
    }


    // 단일 재고 복구
    public void restoreStock(UUID orderUUID, UUID productUUID, int quantity) {
        try {
            Product product = productRepository.findByProductUUIDWithLock(productUUID)
                    .orElseThrow(() -> new IllegalArgumentException("cannot find product: " + productUUID));

            product.restoreStock(quantity);

            eventPublisher.publishStockRestored(orderUUID, productUUID, quantity, product.getStockQuantity());

        }catch (Exception e) {
            log.error("Fail to restore stock: productUUID={}, error={}", productUUID, e.getMessage());
            throw new RuntimeException("Fail to restore stock");
        }
    }

    // 여러 상품 재고 복구
    public void restoreStocks(List<StockDeductRequest> requests, UUID orderUUID) {
        for (StockDeductRequest request : requests) {
            restoreStock(orderUUID, request.getProductUUID(), request.getQuantity());
        }
    }

}
