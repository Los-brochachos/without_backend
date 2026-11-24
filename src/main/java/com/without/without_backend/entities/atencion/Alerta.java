package com.without.without_backend.entities.atencion;

import java.time.LocalDateTime;

import com.without.without_backend.utils.atencion.EstadoAlerta;
import com.without.without_backend.utils.atencion.TipoAlerta;

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
@Table(name = "alerta")
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAlerta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoAlerta tipo;

    @Column(nullable = false, length = 250)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAlerta estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAtencion", nullable = false)
    private Atencion atencion;
}