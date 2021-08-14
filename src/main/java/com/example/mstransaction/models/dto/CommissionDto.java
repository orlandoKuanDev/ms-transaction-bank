package com.example.mstransaction.models.dto;

import com.example.mstransaction.models.entities.Customer;
import com.example.mstransaction.models.entities.Product;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CommissionDto {
    private Double commission;
    private Customer customer;
    private Product product;
    private LocalDateTime commissionDate;
}
