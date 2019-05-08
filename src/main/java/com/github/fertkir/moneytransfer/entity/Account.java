package com.github.fertkir.moneytransfer.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class Account {
    private final Long id;
    private final BigDecimal balance;
}
