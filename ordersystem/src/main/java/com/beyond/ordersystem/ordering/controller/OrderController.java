package com.beyond.ordersystem.ordering.controller;
import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import com.beyond.ordersystem.ordering.dto.OrderListResDto;
import com.beyond.ordersystem.ordering.dto.OrderSaveReqDto;
import com.beyond.ordersystem.ordering.repository.OrderRepository;
import com.beyond.ordersystem.ordering.service.OrderService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController {
    private final OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @PostMapping("/order/create")
    public ResponseEntity<?> orderCreate(@RequestBody List<OrderSaveReqDto> dtos) {
        Ordering ordering = orderService.createOrder(dtos);
        System.out.println(dtos);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "successfully created", ordering.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order/list")
    public ResponseEntity<?> orderRead() {
        List<OrderListResDto> orderList = orderService.listOrder();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "successfully found", orderList);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    // 내 주문만 볼 수 있는 myOrders : order/myorders
    @GetMapping("/order/myorders")
    public ResponseEntity<?> myOrders() {
        List<OrderListResDto> dtos = orderService.myOrders();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }



    // admin사용자의 주문 취소 : /order/{id}/cancel -> orderstatus만 변경

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/order/{id}/cancel")
    public ResponseEntity<?> orderDelete(@PathVariable Long id) {
        Ordering order = orderService.cancelOrder(id);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "successfully canceled", order.getId());
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
