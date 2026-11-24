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
@Table (name = "factura")
@NoArgsConstructor 
@AllArgsConstructor 
public class Factura {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer idFactura;
    @Column (nullable = false, length = 50)
    private String numero;
    @Column (nullable = false)
    private LocalDate fechaEmision;
    @Column (nullable = false, length = 30)
    private String tipoComprobante;
    @Column (nullable = false, length = 10)
    private String moneda;
    @ManyToOne (fetch=FetchType.LAZY)
    @JoinColumn (name = "idPedido", nullable = false)
    private Pedido pedido;
}
