//package com.beyond.ordersystem.ordering.service;
//
////import com.beyond.ordersystem.common.configs.RabbitMqConfig;
//import com.beyond.ordersystem.ordering.dto.StockDecreaseEvent;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//@Component
//public class StockDecreaseEventHandler {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    public void publish(StockDecreaseEvent event) {
//        rabbitTemplate.convertAndSend(RabbitMqConfig.STOCK_DECREASE_QUEUE, event);
//    }
//
////    @Transactional
////    @RabbitListener(queues = RabbitMqConfig.STOCK_DECREASE_QUEUE)
////    public void listen() {
////
////    }
//}
