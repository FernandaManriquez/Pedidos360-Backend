package com.duoc.servicioKafkaAdmin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearTopicRequest(

        @NotBlank
        @Size(max = 249)
        @Pattern(regexp = "pedidos360\\.[a-zA-Z0-9._-]+")
        String nombre,

        @NotNull
        @Min(1)
        @Max(100)
        Integer particiones,

        @NotNull
        @Min(1)
        @Max(3)
        Integer replicas,

        @NotNull
        @Min(60000)
        Long retencionMs,

        @NotBlank
        @Pattern(regexp = "delete|compact|compact,delete")
        String politicaLimpieza
) {
}