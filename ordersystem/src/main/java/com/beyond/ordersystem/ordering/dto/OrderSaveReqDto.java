package com.beyond.ordersystem.ordering.dto;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSaveReqDto {
    private Long productId;
    private int productCount;
//    private Long memberId;
//    private List<OrderDetailReqDto> orderDetailReqDtos;

//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Builder
//    public static class OrderDetailReqDto {
//        private Long productId;
//        private int productCount;
//    }

    public Ordering toEntity(Member member){
        return Ordering.builder()
                .member(member)
                .build();
    }

}
