package com.without.without_backend.entities.cisterna;

import java.time.LocalDateTime;

import com.without.without_backend.entities.trabajador.Tecnico;
import com.without.without_backend.utils.cisterna.EstadoFalla;

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
@Table (name = "falla")
@NoArgsConstructor 
@AllArgsConstructor 
public class Falla {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer idFalla;
    @Column (nullable=false, length=250)
    private String descripcion; 
    @Column (nullable = false)
    private LocalDateTime fechaHora;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoFalla estado;
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "idCisterna", nullable = false)
    private Cisterna cisterna;
    @ManyToOne (fetch=FetchType.LAZY)
    @JoinColumn (name = "idTecnico", nullable = false) 
    private Tecnico tecnico;
}
