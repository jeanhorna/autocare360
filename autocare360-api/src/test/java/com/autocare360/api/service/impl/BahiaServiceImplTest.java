package com.autocare360.api.service.impl;

import com.autocare360.api.dao.BahiaDao;
import com.autocare360.api.dto.request.BahiaRequest;
import com.autocare360.api.dto.response.BahiaResponse;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.mapper.BahiaMapper;
import com.autocare360.api.model.Bahia;
import com.autocare360.api.model.enums.EstadoBahia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BahiaServiceImplTest {

    @Mock
    private BahiaDao bahiaDao;

    @Spy
    private BahiaMapper bahiaMapper = new BahiaMapper();

    @InjectMocks
    private BahiaServiceImpl bahiaService;

    @Test
    void creaBahiaDisponiblePorDefecto() {
        when(bahiaDao.existsByNombreBahiaIgnoreCase("Bahia 5")).thenReturn(false);
        when(bahiaDao.save(any(Bahia.class))).thenAnswer(inv -> {
            Bahia b = inv.getArgument(0);
            b.setIdBahia(5);
            return b;
        });

        BahiaResponse response = bahiaService.crear(new BahiaRequest("Bahia 5", null));

        assertThat(response.idBahia()).isEqualTo(5);
        assertThat(response.estado()).isEqualTo(EstadoBahia.DISPONIBLE);
    }

    @Test
    void rechazaNombreDuplicado() {
        when(bahiaDao.existsByNombreBahiaIgnoreCase("Bahia 1")).thenReturn(true);

        assertThatThrownBy(() -> bahiaService.crear(new BahiaRequest("Bahia 1", null)))
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", CodigoError.BAHIA_DUPLICADA);
    }

    @Test
    void cambiaEstadoAMantenimiento() {
        Bahia bahia = Bahia.builder().idBahia(1).nombreBahia("Bahia 1").estado(EstadoBahia.DISPONIBLE).build();
        when(bahiaDao.findById(1)).thenReturn(Optional.of(bahia));

        BahiaResponse response = bahiaService.cambiarEstado(1, EstadoBahia.MANTENIMIENTO);

        assertThat(response.estado()).isEqualTo(EstadoBahia.MANTENIMIENTO);
    }
}