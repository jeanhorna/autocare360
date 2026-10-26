package com.autocare360.api.service.impl;

import com.autocare360.api.dao.CitaDao;
import com.autocare360.api.dao.UsuarioDao;
import com.autocare360.api.dao.VehiculoDao;
import com.autocare360.api.dto.UsuarioSesion;
import com.autocare360.api.dto.request.VehiculoRequest;
import com.autocare360.api.dto.response.VehiculoResponse;
import com.autocare360.api.exception.AccesoDenegadoException;
import com.autocare360.api.exception.CodigoError;
import com.autocare360.api.exception.NegocioException;
import com.autocare360.api.mapper.VehiculoMapper;
import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.Vehiculo;
import com.autocare360.api.model.enums.EstadoCita;
import com.autocare360.api.model.enums.Rol;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceImplTest {

    @Mock
    private VehiculoDao vehiculoDao;
    @Mock
    private UsuarioDao usuarioDao;
    @Mock
    private CitaDao citaDao;
    @Spy
    private VehiculoMapper vehiculoMapper = new VehiculoMapper();
    @InjectMocks
    private VehiculoServiceImpl vehiculoService;

    private Usuario cliente;
    private Vehiculo vehiculo;
    private final UsuarioSesion sesionCliente = new UsuarioSesion(1, Rol.CLIENTE);
    private final VehiculoRequest request = new VehiculoRequest("abc 123", "Toyota", "Yaris", 2020, 45000);

    @BeforeEach
    void setUp() {
        cliente = Usuario.builder().idUsuario(1).nombre("Ana").apellido("Torres")
                .correo("ana@test.com").contrasena("x").rol(Rol.CLIENTE).build();
        vehiculo = Vehiculo.builder().placa("ABC-123").usuario(cliente).marca("Toyota")
                .modelo("Yaris").anio(2020).kilometraje(45000).activo(true).build();
    }

    @Test
    void registraVehiculoNuevoNormalizandoPlaca() {
        when(usuarioDao.findById(1)).thenReturn(Optional.of(cliente));
        when(vehiculoDao.findById("ABC-123")).thenReturn(Optional.empty());
        when(vehiculoDao.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculoResponse response = vehiculoService.registrar(request, sesionCliente);

        assertThat(response.placa()).isEqualTo("ABC-123");
        assertThat(response.propietario()).isEqualTo("Ana Torres");
    }

    @Test
    void rechazaPlacaDuplicadaActiva() {
        when(usuarioDao.findById(1)).thenReturn(Optional.of(cliente));
        when(vehiculoDao.findById("ABC-123")).thenReturn(Optional.of(vehiculo));

        assertThatThrownBy(() -> vehiculoService.registrar(request, sesionCliente))
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", CodigoError.PLACA_DUPLICADA);
    }

    @Test
    void reactivaVehiculoDadoDeBaja() {
        vehiculo.setActivo(false);
        vehiculo.setMarca("Antigua");
        when(usuarioDao.findById(1)).thenReturn(Optional.of(cliente));
        when(vehiculoDao.findById("ABC-123")).thenReturn(Optional.of(vehiculo));
        when(vehiculoDao.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        vehiculoService.registrar(request, sesionCliente);

        assertThat(vehiculo.getActivo()).isTrue();
        assertThat(vehiculo.getMarca()).isEqualTo("Toyota");
    }

    @Test
    void rechazaPlacaConFormatoInvalido() {
        VehiculoRequest invalido = new VehiculoRequest("AB12", "Kia", "Rio", 2019, 1000);

        assertThatThrownBy(() -> vehiculoService.registrar(invalido, sesionCliente))
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", CodigoError.PLACA_INVALIDA);
    }

    @Test
    void noPermiteDarDeBajaConCitasActivas() {
        when(vehiculoDao.findByPlacaAndActivoTrue("ABC-123")).thenReturn(Optional.of(vehiculo));
        when(citaDao.existsByVehiculoPlacaAndEstadoCitaIn("ABC-123", EstadoCita.ACTIVAS)).thenReturn(true);

        assertThatThrownBy(() -> vehiculoService.darDeBaja("ABC-123", sesionCliente))
                .isInstanceOf(NegocioException.class)
                .hasFieldOrPropertyWithValue("codigoError", CodigoError.VEHICULO_CON_CITAS_ACTIVAS);
        assertThat(vehiculo.getActivo()).isTrue();
    }

    @Test
    void daDeBajaVehiculoSinCitasActivas() {
        when(vehiculoDao.findByPlacaAndActivoTrue("ABC-123")).thenReturn(Optional.of(vehiculo));
        when(citaDao.existsByVehiculoPlacaAndEstadoCitaIn("ABC-123", EstadoCita.ACTIVAS)).thenReturn(false);

        vehiculoService.darDeBaja("abc-123", sesionCliente);

        assertThat(vehiculo.getActivo()).isFalse();
    }

    @Test
    void clienteNoPuedeVerVehiculoAjeno() {
        when(vehiculoDao.findByPlacaAndActivoTrue("ABC-123")).thenReturn(Optional.of(vehiculo));

        assertThatThrownBy(() -> vehiculoService.obtener("ABC-123", new UsuarioSesion(2, Rol.CLIENTE)))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void recepcionistaPuedeVerCualquierVehiculo() {
        when(vehiculoDao.findByPlacaAndActivoTrue("ABC-123")).thenReturn(Optional.of(vehiculo));

        VehiculoResponse response = vehiculoService.obtener("ABC-123", new UsuarioSesion(9, Rol.RECEPCIONISTA));

        assertThat(response.placa()).isEqualTo("ABC-123");
    }
}