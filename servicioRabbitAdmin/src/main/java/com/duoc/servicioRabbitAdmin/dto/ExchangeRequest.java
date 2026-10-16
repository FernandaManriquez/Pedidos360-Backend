package com.duoc.servicioRabbitAdmin.dto;

import jakarta.validation.constraints.*;

public record ExchangeRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255)
        @Pattern(
                regexp = RabbitValidation.NAME_PATTERN,
                message = "Nombre invalido o reservado"
        )
        String name,

        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(
                regexp = "direct|topic|fanout",
                message = "El tipo debe ser direct, topic o fanout"
        )
        String type,

        @NotNull(message = "durable es obligatorio")
        Boolean durable,

        @NotNull(message = "autoDelete es obligatorio")
        Boolean autoDelete
) {
}
