package com.without.without_backend.entities.documento;

import java.time.LocalDate;

import com.without.without_backend.entities.pedido.Pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table (name = "guia_remision")
@NoArgsConstructor 
@AllArgsConstructor 
public class GuiaRemision {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer idGuia;
    @Column (nullable = false, length = 50)
    private String numero;
    @Column (nullable = false)
    private LocalDate fechaEmision;
    @Column (nullable = false, length = 100)
    private String motivoTraslado;
    @Column (nullable = false, length = 150)
    private String puntoPartida;
    @Column (nullable = false, length = 150)
    private String puntoLlegada;
    @Column 
    private LocalDate fechaInicioTraslado;
    @ManyToOne (fetch=FetchType.LAZY)
    @JoinColumn (name = "idPedido")
    private Pedido pedido;
}
