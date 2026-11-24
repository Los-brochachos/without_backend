package com.without.without_backend.entities.trabajador;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.without.without_backend.entities.usuario.Usuario;
import com.without.without_backend.utils.Estado;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "trabajador")
@NoArgsConstructor
@AllArgsConstructor
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTrabajador;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 20, unique = true)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String direccion;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    @OneToOne(mappedBy = "trabajador", fetch = FetchType.LAZY)
    private Conductor conductor;

    @OneToOne(mappedBy = "trabajador", fetch = FetchType.LAZY)
    private Tecnico tecnico;

    @OneToOne(mappedBy = "trabajador", fetch = FetchType.LAZY)
    private Programador programador;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}


