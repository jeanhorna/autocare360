package com.autocare360.api.model;

import com.autocare360.api.model.enums.EstadoBahia;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bahia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Bahia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bahia")
    private Integer idBahia;

    @Column(name = "nombre_bahia", nullable = false, unique = true, length = 50)
    private String nombreBahia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoBahia estado;

    public boolean estaOperativa() {
        return estado != EstadoBahia.MANTENIMIENTO;
    }
}