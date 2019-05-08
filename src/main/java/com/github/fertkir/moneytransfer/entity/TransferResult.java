package com.github.fertkir.moneytransfer.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferResult {
    private final Account source;
    private final Account target;
}
