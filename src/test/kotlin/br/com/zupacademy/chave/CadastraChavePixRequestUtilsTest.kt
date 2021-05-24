package br.com.zupacademy.chave

import br.com.zupacademy.TipoChaveRequest
import br.com.zupacademy.TipoContaRequest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

internal class CadastraChavePixRequestUtilsTest {
    @Nested
    inner class TipoChaveRequestToModel {

        @Test
        fun deveRetornarTipoChaveCpf() {
            val tipoChave = TipoChaveRequest.TIPO_CHAVE_CPF.toModel()
            assertEquals(TipoChave.CPF, tipoChave)
        }

        @Test
        fun deveRetornarTipoChaveEmail() {
            val tipoChave = TipoChaveRequest.TIPO_CHAVE_EMAIL.toModel()
            assertEquals(TipoChave.EMAIL, tipoChave)
        }

        @Test
        fun deveRetornarTipoChaveTelefone() {
            val tipoChave = TipoChaveRequest.TIPO_CHAVE_TELEFONE.toModel()
            assertEquals(TipoChave.TELEFONE, tipoChave)
        }

        @Test
        fun deveRetornarTipoChaveAleatoria() {
            val tipoChave = TipoChaveRequest.TIPO_CHAVE_ALEATORIA.toModel()
            assertEquals(TipoChave.ALEATORIA, tipoChave)
        }

        @Test
        fun deveRetornarNulo() {
            val tipoChave = TipoChaveRequest.TIPO_CHAVE_UNSPECIFIED.toModel()
            assertEquals(null, tipoChave)
        }
    }

    @Nested
    inner class TipoContaRequestToModel {
        @Test
        fun deveRetornatTipoContaCorrente() {
            val tipoConta = TipoContaRequest.TIPO_CONTA_CORRENTE.toModel()
            assertEquals(TipoConta.CORRENTE, tipoConta)
        }

        @Test
        fun deveRetornatTipoContaPoupanca() {
            val tipoConta = TipoContaRequest.TIPO_CONTA_POUPANCA.toModel()
            assertEquals(TipoConta.POUPANCA, tipoConta)
        }

        @Test
        fun deveRetornatNulo() {
            val tipoConta = TipoContaRequest.TIPO_CONTA_UNSPECIFIED.toModel()
            assertEquals(null, tipoConta)
        }
    }
}