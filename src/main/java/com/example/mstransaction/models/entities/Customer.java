package com.example.mstransaction.models.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Customer {
    @Field(name = "customerIdentityNumber")
    private String customerIdentityNumber;
}
