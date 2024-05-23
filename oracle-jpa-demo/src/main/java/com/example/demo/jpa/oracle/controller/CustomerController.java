package com.example.demo.jpa.oracle.controller;

import com.example.demo.jpa.oracle.entity.Customer;
import com.example.demo.jpa.oracle.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController(value = "api")
@RequestMapping(path = "api/")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping(value = "customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        final List<Customer> customers = customerService.getAllCustomer();
        return ResponseEntity.ok(customers);
    }

}
