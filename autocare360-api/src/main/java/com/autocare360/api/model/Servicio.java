package com.autocare360.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "nombre_servicio", nullable = false, unique = true, length = 100)
    private String nombreServicio;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "costo_estimado", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "duracion_estimada_min", nullable = false)
    private Integer duracionEstimadaMin;
}
