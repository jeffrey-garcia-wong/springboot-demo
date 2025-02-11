package com.example.demo.k8s.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Clob;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "HTTPBINDATA")
public class HttpBinData {
    @Id
    String id;
    @Column(name = "JSON")
    Clob jsonString;
}
