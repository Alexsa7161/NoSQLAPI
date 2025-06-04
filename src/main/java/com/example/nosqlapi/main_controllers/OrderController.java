package com.example.nosqlapi.main_controllers;


import com.example.nosqlapi.main_entity.Order;
import com.example.nosqlapi.main_entity.OrderEmployeeRequestDTO;
import com.example.nosqlapi.main_services.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order, @RequestParam UUID employeeId) {
        order.setEmployee_id(employeeId);
        Order created = orderService.createOrder(order, employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{orderNumber}")
    public ResponseEntity<Order> updateOrder(@PathVariable String orderNumber, @RequestBody Order order) {
        Order updated = orderService.updateOrder(orderNumber, order);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{orderNumber}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderNumber) {
        orderService.deleteOrder(orderNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderNumber) {
        Order order = orderService.getOrder(orderNumber);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }


        @GetMapping("/aggregated/{orderNumber}")
        public ResponseEntity<OrderEmployeeRequestDTO> getAggregatedOrderData(@PathVariable String orderNumber) throws JsonProcessingException {
            UUID orderUUID = UUID.fromString(orderNumber);
            OrderEmployeeRequestDTO dto = orderService.getAggregatedOrderData(orderUUID);
            return ResponseEntity.ok(dto);
        }
}
