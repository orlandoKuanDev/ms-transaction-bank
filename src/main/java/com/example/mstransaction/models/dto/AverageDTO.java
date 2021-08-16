package com.example.mstransaction.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AverageDTO {
    private List<BalanceDTO> balances;
    private Double average;
}
