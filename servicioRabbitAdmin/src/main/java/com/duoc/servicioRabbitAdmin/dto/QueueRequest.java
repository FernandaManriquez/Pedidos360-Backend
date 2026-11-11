package com.duoc.servicioRabbitAdmin.dto;

import jakarta.validation.constraints.*;

public record QueueRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255)
        @Pattern(
                regexp = RabbitValidation.NAME_PATTERN,
                message = "Nombre invalido o reservado"
        )
        String name,

        @NotNull(message = "durable es obligatorio")
        Boolean durable,

        @NotNull(message = "autoDelete es obligatorio")
        Boolean autoDelete
) {
}
