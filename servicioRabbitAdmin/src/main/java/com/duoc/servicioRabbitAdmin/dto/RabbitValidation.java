package com.duoc.servicioRabbitAdmin.dto;

public final class RabbitValidation {
    public static final String NAME_PATTERN =
            "^(?!amq\\.)[A-Za-z0-9][A-Za-z0-9._-]*$";

    public static final String ROUTING_KEY_PATTERN =
            "^[A-Za-z0-9._*#-]*$";

    private RabbitValidation() {
    }
}
