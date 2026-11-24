package com.without.without_backend.utils.pedido;

public enum EstadoPedido {


    RECIBIDO(1),
    EN_PREPARACION(2),
    DESPACHADO(3),
    ENTREGADO(4),
    CANCELADO(0);

    private final int orden;


    EstadoPedido(int orden) {
        this.orden = orden;
    }


    public boolean esSiguienteA(EstadoPedido estadoActual) {
        return this.orden == estadoActual.orden+1;
    }
}