package com.without.without_backend.entities.pedido;

import java.time.LocalDate;
import java.util.List;

import com.without.without_backend.entities.cliente.Cliente;
import com.without.without_backend.utils.pedido.EstadoPedido;
import com.without.without_backend.utils.pedido.PrioridadPedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@Table(name = "pedido")
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPedido;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    @Column
    private LocalDate fechaEntregaEstimada;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PrioridadPedido prioridad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", nullable = false)
    private Cliente cliente;

        @OneToMany(
        mappedBy = "pedido",
        fetch = FetchType.LAZY
    )
    private List<DetallePedido> detalles;
}