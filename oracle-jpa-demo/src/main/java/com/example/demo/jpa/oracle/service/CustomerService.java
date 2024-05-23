package com.example.demo.jpa.oracle.service;

import com.example.demo.jpa.oracle.entity.Customer;
import com.example.demo.jpa.oracle.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomer() {
        return customerRepository.findAll();
    }

}
