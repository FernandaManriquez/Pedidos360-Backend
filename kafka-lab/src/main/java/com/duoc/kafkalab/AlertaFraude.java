package com.duoc.kafkalab;

import java.math.BigDecimal;

public record AlertaFraude(
        String usuario,
        BigDecimal monto,
        String motivo,
        String severidad,
        Long timestamp
) {}
