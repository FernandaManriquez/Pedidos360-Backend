package com.duoc.servicioRabbitAdmin.dto;

import jakarta.validation.constraints.*;

public record BindingRequest(
        @NotBlank(message = "La cola es obligatoria")
        @Size(max = 255)
        @Pattern(
                regexp = RabbitValidation.NAME_PATTERN,
                message = "Nombre de cola invalido"
        )
        String queue,

        @NotBlank(message = "El exchange es obligatorio")
        @Size(max = 255)
        @Pattern(
                regexp = RabbitValidation.NAME_PATTERN,
                message = "Nombre de exchange invalido"
        )
        String exchange,

        @NotNull(message = "routingKey es obligatorio")
        @Size(max = 255)
        @Pattern(
                regexp = RabbitValidation.ROUTING_KEY_PATTERN,
                message = "Clave de enrutamiento invalida"
        )
        String routingKey
) {
}
