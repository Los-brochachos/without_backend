package com.without.without_backend.entities.trabajador;

import com.without.without_backend.utils.trabajador.EspecialidadTecnico;
import com.without.without_backend.utils.trabajador.NivelTecnico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Builder 
@Getter 
@Setter 
@Table (name="tecnico")
@NoArgsConstructor 
@AllArgsConstructor 
public class Tecnico {
    @Id 
    private Integer idTrabajador;
    @OneToOne (fetch= FetchType.LAZY)
    @MapsId 
    @JoinColumn (name = "idTrabajador")
    private Trabajador trabajador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EspecialidadTecnico especialidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private NivelTecnico nivelTecnico;
    
}
