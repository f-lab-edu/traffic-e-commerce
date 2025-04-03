package com.ecommerce.product.productController;

import com.ecommerce.product.dto.request.ProductDeleteRequest;
import com.ecommerce.product.dto.request.ProductModifyRequest;
import com.ecommerce.product.dto.request.ProductRegisterRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.dto.search.DetailedSearchCondition;
import com.ecommerce.product.productEntity.Product;
import com.ecommerce.product.productService.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class productController {

    private final ProductService productService;


    /**
     * 판매자: 전체 상품 조회
     * @params
     * auth : jwt 토큰
     **/
    @GetMapping("/seller/getProducts")
    public ResponseEntity<List<ProductResponse>> getSellerProducts(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");
        String email = "";
        Long sellerId = Long.valueOf("");
        List<ProductResponse> products = productService.getSellerProducts(sellerId);
        return ResponseEntity.ok(products);
    }


    /**
     * 판매자: 상품목록 조회
     * @params
     * auth : jwt 토큰
     **/
    @GetMapping("/seller/getProducts")
    public ResponseEntity<List<ProductResponse>> getSellerProductsByStatus(@RequestHeader("Authorization") String auth, String status) {
        String token = auth.replace("Bearer ", "");
        String email = "";
        Long sellerId = Long.valueOf("");
        List<ProductResponse> products = productService.getSellerProducts(sellerId, status);
        return ResponseEntity.ok(products);
    }


    /**
     * 판매자: 상품 등록
     * @params
     * auth : jwt 토큰
     * request : 상품 등록
     **/
    @PostMapping("/register")
    public ResponseEntity<?> registerProduct(@RequestHeader("Authorization") String auth, @RequestBody @Validated ProductRegisterRequest request) {
        String token = auth.replace("Bearer ", "");
        Long sellerId = Long.valueOf("");
        Product product = productService.registerProduct(sellerId, request);
        return ResponseEntity.ok(product);
    }


    /**
     * 판매자: 상품 정보 변경
     * @params
     * auth : jwt 토큰
     * request : 상품 변경 사항
     **/
    @PostMapping("/modify/{productId}")
    public Product modifyProduct(@RequestHeader("Authorization") String auth, @Validated ProductModifyRequest request) throws AccessDeniedException {
        String token = auth.replace("Bearer ", "");
        Long sellerId = Long.valueOf("");
        Long productId = Long.valueOf("");
        return productService.modifyProduct(sellerId, productId, request);
    }


    /**
     * 판매자: 등록된 상품 삭제
     * @params
     * auth : jwt 토큰
     * request : 상품 삭제 대상
     **/
    @DeleteMapping("/delete/{productId}")
    public void deleteProduct(@RequestHeader("Authorization") String auth, @Validated ProductDeleteRequest request) throws AccessDeniedException {
        String token = auth.replace("Bearer ", "");
        Long sellerId = Long.valueOf("");
        productService.deleteProduct(sellerId, request.getProductId());
    }


    /**
     * 구매자: 상품 검색 : 상세정보
     * @params
     * auth : jwt 토큰
     * productId : 상품 id
     **/
    @GetMapping("/detail/{productId}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable Long productId) {
        ProductResponse detailedProduct = productService.getProductDetail(productId);
        return ResponseEntity.ok(detailedProduct);
    }

    /**
     * 구매자: 상품 검색 : 상세 파라미터
     * @params
     * auth : jwt 토큰
     * name : 상품 이름
     * categoryId : 카테고리 id
     * minPrice: 최소값
     * maxPrice: 최대값
     * pageable: 페이징 처리 단위
     **/
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String name
            , @RequestParam(required = false) Long categoryId
            , @RequestParam(required = false) BigDecimal minPrice
            , @RequestParam(required = false) BigDecimal maxPrice
            , @PageableDefault(size = 20) Pageable pageable
    ) {

        DetailedSearchCondition searchCondition = DetailedSearchCondition.of(name, categoryId, minPrice, maxPrice, pageable);

        Page<ProductResponse> searchedProducts = productService.detailSearchProduct(searchCondition);
        return ResponseEntity.ok(searchedProducts);
    }


}
