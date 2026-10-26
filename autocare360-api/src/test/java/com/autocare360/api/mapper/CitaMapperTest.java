package com.autocare360.api.mapper;

import com.autocare360.api.dto.response.CitaResponse;
import com.autocare360.api.model.*;
import com.autocare360.api.model.enums.EstadoBahia;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.model.enums.Rol;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CitaMapperTest {

    private final CitaMapper mapper = new CitaMapper();

    @Test
    void mapeaCitaAResponseCalculandoHoraFin() {
        Usuario usuario = Usuario.builder().idUsuario(1).nombre("Ana").apellido("Torres")
                .correo("ana@test.com").contrasena("x").rol(Rol.CLIENTE).build();
        Vehiculo vehiculo = Vehiculo.builder().placa("ABC-123").usuario(usuario)
                .marca("Toyota").modelo("Yaris").anio(2020).kilometraje(45000).build();
        Servicio servicio = Servicio.builder().idServicio(2).nombreServicio("Revision de frenos")
                .costoEstimado(new BigDecimal("150.00")).duracionEstimadaMin(60).build();
        Bahia bahia = Bahia.builder().idBahia(3).nombreBahia("Bahia 3").estado(EstadoBahia.DISPONIBLE).build();

        Cita cita = Cita.builder().idCita(10).codigoCita("AC-20261027-AB12")
                .usuario(usuario).vehiculo(vehiculo).servicio(servicio).bahia(bahia)
                .fechaHoraProgramada(LocalDateTime.of(2026, 10, 27, 9, 0))
                .estadoCita(EstadoCita.PENDIENTE).build();

        CitaResponse response = mapper.toResponse(cita);

        assertThat(response.codigoCita()).isEqualTo("AC-20261027-AB12");
        assertThat(response.fechaHoraFin()).isEqualTo(LocalDateTime.of(2026, 10, 27, 10, 0));
        assertThat(response.cliente()).isEqualTo("Ana Torres");
        assertThat(response.vehiculo()).isEqualTo("Toyota Yaris");
        assertThat(response.nombreBahia()).isEqualTo("Bahia 3");
        assertThat(response.estadoCita()).isEqualTo(EstadoCita.PENDIENTE);
    }
}