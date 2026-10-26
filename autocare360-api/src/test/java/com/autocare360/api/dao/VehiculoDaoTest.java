package com.autocare360.api.dao;

import com.autocare360.api.model.Usuario;
import com.autocare360.api.model.Vehiculo;
import com.autocare360.api.model.enums.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VehiculoDaoTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private VehiculoDao vehiculoDao;

    @Test
    void listaSoloVehiculosActivosDelPropietario() {
        Usuario duenio = em.persist(Usuario.builder().nombre("Luis").apellido("Rojas")
                .correo("luis@test.com").contrasena("hash").rol(Rol.CLIENTE).build());
        Usuario otro = em.persist(Usuario.builder().nombre("Eva").apellido("Diaz")
                .correo("eva@test.com").contrasena("hash").rol(Rol.CLIENTE).build());

        em.persist(Vehiculo.builder().placa("AAA-111").usuario(duenio).marca("Kia")
                .modelo("Rio").anio(2019).kilometraje(10000).build());
        em.persist(Vehiculo.builder().placa("BBB-222").usuario(duenio).marca("Hyundai")
                .modelo("Accent").anio(2018).kilometraje(20000).activo(false).build());
        em.persist(Vehiculo.builder().placa("CCC-333").usuario(otro).marca("Nissan")
                .modelo("Versa").anio(2021).kilometraje(5000).build());
        em.flush();

        assertThat(vehiculoDao.findByUsuarioIdUsuarioAndActivoTrueOrderByMarcaAscModeloAsc(duenio.getIdUsuario()))
                .extracting(Vehiculo::getPlaca).containsExactly("AAA-111");
        assertThat(vehiculoDao.findByPlacaAndActivoTrue("BBB-222")).isEmpty();
    }
}