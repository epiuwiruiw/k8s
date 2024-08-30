package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.ordering.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderListResDto {
    private Long id; // 주문 번호
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailResDto> orderDetailResDtos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderDetailResDto {
        private Long id;
        private String productName;
        private Integer count;
    }
}
