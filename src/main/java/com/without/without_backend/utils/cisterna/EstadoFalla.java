package com.without.without_backend.utils.cisterna;

public enum EstadoFalla {

    PENDIENTE(1),
    EN_REPARACION(2),
    RESUELTA(3);

    private final int orden;

    EstadoFalla(int orden) {
        this.orden = orden;
    }

    public int getOrden() {
        return orden;
    }
}