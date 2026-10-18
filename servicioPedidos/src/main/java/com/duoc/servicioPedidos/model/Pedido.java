package com.duoc.servicioPedidos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(max = 150)
    private String usuario;

    @NotNull(message = "El producto es obligatorio")
    @Positive(message = "El producto debe ser positivo")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero(message = "El total no puede ser negativo")
    private Integer total;

    private String estado;

    private LocalDateTime fecha;

    @PrePersist
    public void prePersist() {

        if (fecha == null) {
            fecha = LocalDateTime.now();
        }

        if (estado == null) {
            estado = "PENDIENTE";
        }
    }
}