package com.duoc.servicioKafkaAdmin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ActualizarTopicConfigRequest(
        @Min(60000)
        Long retencionMs,

        @Pattern(regexp = "delete|compact|compact,delete")
        String politicaLimpieza
) {
}
