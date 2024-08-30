package com.beyond.ordersystem.ordering.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
import com.beyond.ordersystem.ordering.repository.OrderDetailRepository;
import com.beyond.ordersystem.ordering.repository.OrderRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final StockInventoryService stockInventoryService;
//    private final StockDecreaseEventHandler stockDecreaseEventHandler;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        MemberRepository memberRepository,
                        ProductRepository productRepository,
                        OrderDetailRepository orderDetailRepository,
                        StockInventoryService stockInventoryService
                        ) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.stockInventoryService = stockInventoryService;
//        this.stockDecreaseEventHandler = stockDecreaseEventHandler;
    }


//    public Ordering createOrder(OrderSaveReqDto dto) {
//
//        //        방법1.쉬운방식
////        Ordering생성 : member_id, status
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("없음"));
//        Ordering ordering = orderRepository.save(dto.toEntity(member));
//
////        OrderDetail생성 : order_id, product_id, quantity
//        for(OrderSaveReqDto.OrderDetailReqDto orderDto : dto.getOrderDetailReqDtos()){
//            Product product = productRepository.findById(orderDto.getProductId()).orElse(null);
//            int quantity = orderDto.getProductCount();
//            OrderDetail orderDetail =  OrderDetail.builder()
//                    .product(product)
//                    .quantity(quantity)
//                    .ordering(ordering)
//                    .build();
//            orderDetailRepository.save(orderDetail);
//        }


//
//        // 방법2. JPA에 최적화된 방식
//        Member member = memberRepository.findById(dto.getMemberId()).orElseThrow(()->new EntityNotFoundException("not found"));
//        Ordering ordering = Ordering.builder()
////                .orderDetails(orderDetailList)
//                .member(member)
//                .build();
//
//        for(OrderSaveReqDto.OrderDetailDto orderDetailDto : dto.getOrderDetailDtoList()) {
//            Product product = productRepository.findById(orderDetailDto.getProductId()).orElseThrow(()->new EntityNotFoundException("not found"));
//            OrderDetail orderDetail = OrderDetail.builder()
//                    .ordering(ordering)
//                    .product(product)
//                    .quantity(orderDetailDto.getProductCount())
//                    .build();
//            ordering.getOrderDetails().add(orderDetail);
//
//        }
//
//
//        Ordering savedOrdering = orderRepository.save(ordering);
//        return savedOrdering;
//    }

    // synchronized를 설정한다 하더라도, 재고 감소가 DB에 반영되는 시점은 트랜잭션이 커밋되고 종료되는 시점

    public Ordering createOrder(List<OrderSaveReqDto> dtos) { // productId, productCount 가져옴

        // 토큰 받아서
        String memberEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(()-> new EntityNotFoundException("없는 회원입니다."));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        for (OrderSaveReqDto dto : dtos) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()->new EntityNotFoundException("product is not found"));
            int quantity = dto.getProductCount();
            // redis를 통한 재고관리 및 재고잔량 확인
            if(product.getName().contains("sale")) {
                int newQuantity = (stockInventoryService.decreaseStock(dto.getProductId(),dto.getProductCount())).intValue();
                if(newQuantity<0) {
                    throw new IllegalArgumentException("재고부족");
                }
                // rdb에 재고를 업데이트. rabbitmq를 통해 비동기적으로 이벤트 처리.
//                stockDecreaseEventHandler.publish(new StockDecreaseEvent(product.getId(), dto.getProductCount()));
            }else{
                if (product.getStockQuantity() < quantity){
                    throw new IllegalArgumentException("재고부족");
                }
                product.updateStockQuantity(quantity); //변경감지(dirty checking)로 인해 별도의 save 불필요
            }

            OrderDetail orderDetail = OrderDetail.builder()
                    .product(product)
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            ordering.getOrderDetails().add(orderDetail);
        }
        orderRepository.save(ordering);

        return ordering;
    }

    public List<OrderListResDto> listOrder() {
        List<Ordering> orders = orderRepository.findAll();
        List<OrderListResDto> dtos = new ArrayList<>();
        for(Ordering o : orders) {
            dtos.add(o.fromEntity());
        }
        return dtos;
    }

    public List<OrderListResDto> myOrders() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(()->new EntityNotFoundException("없는 이메일입니다"));
        List<Ordering> orders = orderRepository.findByMember(member);
        List<OrderListResDto> dtos = new ArrayList<>();
        for(Ordering o : orders) {
            dtos.add(o.fromEntity());
        }
        return dtos;
    }

    public Ordering cancelOrder(Long id) {
        Ordering order = orderRepository.findById(id).orElseThrow(()->new EntityNotFoundException("존재하지 않는 아이디입니다"));
        order.updateStatus(OrderStatus.CANCELED);
        return order;
    }
}