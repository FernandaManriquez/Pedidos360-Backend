package com.duoc.kafkalab;

import java.math.BigDecimal;

public record Transaccion(
        String usuario,
        BigDecimal monto,
        String origen,
        Long timestamp
) {}
