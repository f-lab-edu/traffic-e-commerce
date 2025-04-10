package com.ecommerce.product.productRepository;

import com.ecommerce.product.productEntity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, String> {

    // 판매자/구매자 : 단일 상품 조회
    Optional<Product> findByProductUUID(UUID uuid);

    // 판매자 : 전체 상품 조회(판매자 관리용)
    List<Product> findAllBySellerId(UUID sellerId);

    // 판매자 : 전체 상품 조회(판매자 관리용)
    List<Product> findBySellerIdAndIsActive(UUID sellerId, boolean isActive);


    // 구매지 : 소비자 상품검색
    @Query("select p from Product p " +
            "where " +
            "(:categoryId is null or p.categoryId.id = :categoryId) and " +
            "(:name IS NULL OR p.name LIKE %:name%) and " +
            "(:minPrice IS NULL OR p.price >= :minPrice) and " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) and " +
            "p.isActive = true and p.isDeleted = false")
    Page<Product> searchProductForCustomer(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // 판매자: 상품 soft 삭제
    Product save(Product product);

}
