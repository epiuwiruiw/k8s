package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.product.domain.Product;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_id")
    private Ordering ordering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    public OrderListResDto.OrderDetailResDto fromEntity(){
        OrderListResDto.OrderDetailResDto dto = OrderListResDto.OrderDetailResDto.builder()
                .id(this.id)
                .productName(this.product.getName())
                .count(this.quantity)
                .build();
        return dto;
    }
}
