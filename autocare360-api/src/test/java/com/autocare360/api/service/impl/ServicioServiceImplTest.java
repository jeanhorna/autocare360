package com.autocare360.api.service.impl;

import com.autocare360.api.dao.ServicioDao;
import com.autocare360.api.dto.request.ServicioRequest;
import com.autocare360.api.dto.response.ServicioResponse;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.exception.RecursoNoEncontradoException;
import com.autocare360.api.mapper.ServicioMapper;
import com.autocare360.api.model.Servicio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceImplTest {

    @Mock
    private ServicioDao servicioDao;

    @Spy
    private ServicioMapper servicioMapper = new ServicioMapper();

    @InjectMocks
    private ServicioServiceImpl servicioService;

    private final ServicioRequest request =
            new ServicioRequest(" Cambio de aceite ", "Aceite y filtro", new BigDecimal("120"), 45);

    @Test
    void creaServicioNormalizandoDatos() {
        when(servicioDao.existsByNombreServicioIgnoreCase("Cambio de aceite")).thenReturn(false);
        when(servicioDao.save(any(Servicio.class))).thenAnswer(inv -> {
            Servicio s = inv.getArgument(0);
            s.setIdServicio(1);
            return s;
        });

        ServicioResponse response = servicioService.crear(request);

        assertThat(response.idServicio()).isEqualTo(1);
        assertThat(response.nombreServicio()).isEqualTo("Cambio de aceite");
        assertThat(response.costoEstimado()).isEqualByComparingTo("120.00");
    }

    @Test
    void rechazaNombreDuplicado() {
        when(servicioDao.existsByNombreServicioIgnoreCase("Cambio de aceite")).thenReturn(true);

        assertThatThrownBy(() -> servicioService.crear(request))
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", CodigoError.SERVICIO_DUPLICADO);
        verify(servicioDao, never()).save(any());
    }

    @Test
    void actualizarServicioInexistenteLanza404() {
        when(servicioDao.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.actualizar(99, request))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminaServicioExistente() {
        Servicio servicio = Servicio.builder().idServicio(1).nombreServicio("Cambio de aceite")
                .costoEstimado(new BigDecimal("120.00")).duracionEstimadaMin(45).build();
        when(servicioDao.findById(1)).thenReturn(Optional.of(servicio));

        servicioService.eliminar(1);

        verify(servicioDao).delete(servicio);
        verify(servicioDao).flush();
    }
}