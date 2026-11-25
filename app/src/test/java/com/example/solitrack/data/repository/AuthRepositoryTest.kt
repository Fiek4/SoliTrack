package com.example.solitrack.data.repository

import com.example.solitrack.data.local.UsuarioDao
import com.example.solitrack.data.local.UsuarioEntity
import com.example.solitrack.data.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class AuthRepositoryTest : StringSpec({

    val admin = UsuarioEntity(id = 2, usuario = "admin", clave = "1234", nombre = "Andrés Soto", rol = Rol.ADMINISTRADOR)

    fun daoFalso() = mockk<UsuarioDao>().also {
        coEvery { it.insertarTodos(any()) } returns Unit
    }

    "login correcto devuelve el usuario con su rol" {
        val dao = daoFalso()
        coEvery { dao.buscarPorCredenciales("admin", "1234") } returns admin

        val usuario = AuthRepository(dao).login("admin", "1234")

        usuario?.rol shouldBe Rol.ADMINISTRADOR
    }

    "login incorrecto devuelve null" {
        val dao = daoFalso()
        coEvery { dao.buscarPorCredenciales(any(), any()) } returns null

        AuthRepository(dao).login("admin", "clave-mala").shouldBeNull()
    }

    "antes de validar se aseguran las cuentas de prueba de los 3 perfiles" {
        val dao = daoFalso()
        coEvery { dao.buscarPorCredenciales(any(), any()) } returns null

        AuthRepository(dao).login("cliente", "1234")

        coVerify(exactly = 1) { dao.insertarTodos(AuthRepository.USUARIOS_PRUEBA) }
        AuthRepository.USUARIOS_PRUEBA.map { it.rol } shouldContainAll Rol.entries
    }
})
