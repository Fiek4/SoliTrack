package com.example.solitrack

import com.example.solitrack.data.model.Categoria
import com.example.solitrack.data.repository.SolicitudRepository
import com.example.solitrack.ui.solicitud.SolicitudValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SolicitudValidatorTest {

    // ---------- Título ----------

    @Test
    fun tituloVacio_retornaError() {
        assertNotNull(SolicitudValidator.validarTitulo(""))
    }

    @Test
    fun tituloSoloEspacios_retornaError() {
        assertNotNull(SolicitudValidator.validarTitulo("     "))
    }

    @Test
    fun tituloMuyCorto_retornaError() {
        assertNotNull(SolicitudValidator.validarTitulo("Web"))
    }

    @Test
    fun tituloValido_noRetornaError() {
        assertNull(SolicitudValidator.validarTitulo("El sitio web no carga"))
    }

    // ---------- Descripción ----------

    @Test
    fun descripcionVacia_retornaError() {
        assertNotNull(SolicitudValidator.validarDescripcion(""))
    }

    @Test
    fun descripcionValida_noRetornaError() {
        assertNull(SolicitudValidator.validarDescripcion("Desde ayer la página de inicio muestra error 500"))
    }

    // ---------- Categoría ----------

    @Test
    fun categoriaVacia_retornaError() {
        assertNotNull(SolicitudValidator.validarCategoria(""))
    }

    @Test
    fun categoriaInexistente_retornaError() {
        assertNotNull(SolicitudValidator.validarCategoria("Categoría inventada"))
    }

    @Test
    fun categoriaValida_noRetornaError() {
        assertNull(SolicitudValidator.validarCategoria(Categoria.HOSTING.etiqueta))
    }

    // ---------- Servicio relacionado ("cuando corresponda") ----------

    @Test
    fun servicioVacio_esObligatorioParaHosting() {
        assertNotNull(SolicitudValidator.validarServicio(Categoria.HOSTING.etiqueta, ""))
    }

    @Test
    fun servicioVacio_esOpcionalParaConsulta() {
        assertNull(SolicitudValidator.validarServicio(Categoria.CONSULTA.etiqueta, ""))
    }

    // ---------- Código único ----------

    @Test
    fun codigoGenerado_tieneFormatoSOL() {
        assertEquals("SOL-0001", SolicitudRepository.generarCodigo(1))
        assertEquals("SOL-0025", SolicitudRepository.generarCodigo(25))
    }
}
