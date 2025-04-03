package com.ecommerce.product.dto.search;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

@Getter
@Builder
@RequiredArgsConstructor
public class DetailedSearchCondition {

    private String name;

    private Long categoryId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Pageable pageable;

    public static DetailedSearchCondition of(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return DetailedSearchCondition.builder()
                .name(name)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .pageable(pageable)
                .build();

    }

}
