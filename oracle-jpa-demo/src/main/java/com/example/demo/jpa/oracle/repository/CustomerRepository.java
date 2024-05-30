package com.example.demo.jpa.oracle.repository;

import com.example.demo.jpa.oracle.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("select c from Customer c where c.name = :name")
    List<Customer> findAllByName(@Param("name") String name);
}