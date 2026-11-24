package com.without.without_backend.entities.pedido;

import java.math.BigDecimal;

import com.without.without_backend.utils.Estado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto")
@Builder 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String tipoGas;

    @Column(nullable = false, length = 30)
    private String unidadMedida;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Enumerated (EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;
}