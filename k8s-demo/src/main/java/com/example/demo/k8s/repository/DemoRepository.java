package com.example.demo.k8s.repository;

import com.example.demo.k8s.model.HttpBinData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemoRepository extends JpaRepository<HttpBinData, String> {
}
