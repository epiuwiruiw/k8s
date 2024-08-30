package com.beyond.ordersystem.ordering.domain;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ordering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @OneToMany(mappedBy = "ordering", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public OrderListResDto fromEntity() {
        List<OrderDetail> orderDetailList = this.getOrderDetails();
        List<OrderListResDto.OrderDetailResDto> dtos = new ArrayList<>();
        for(OrderDetail o : orderDetailList) {
            dtos.add(o.fromEntity());
        }

        OrderListResDto dto = OrderListResDto.builder()
                .id(this.id)
                .memberEmail(this.member.getEmail())
                .orderStatus(this.orderStatus)
                .orderDetailResDtos(dtos)
                .build();

        return dto;
    }


    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
