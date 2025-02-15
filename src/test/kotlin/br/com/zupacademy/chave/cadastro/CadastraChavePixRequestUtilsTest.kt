package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.TipoChaveRequest
import br.com.zupacademy.TipoContaRequest
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import javax.inject.Inject

@Nested
@MicronautTest(transactional = false)
internal class CadastraChavePixRequestToValidatedProxy(@Inject val validator: Validator) {

    private companion object {
        @JvmStatic
        fun chavePixValida() = Stream.of(
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_CPF, "11111111111"),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_EMAIL, "teste@email.com"),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_TELEFONE, "+551111111111"),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_ALEATORIA, "")
        )

        @JvmStatic
        fun chavePixInvalida() = Stream.of(
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_CPF, "", TipoChave.CPF),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_EMAIL, "", TipoChave.EMAIL),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_TELEFONE, "", TipoChave.TELEFONE),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_CPF, "111.111.111-11", TipoChave.CPF),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_EMAIL, "testeemail.com", TipoChave.EMAIL),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_TELEFONE, "1111111111", TipoChave.TELEFONE),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_ALEATORIA, "A", TipoChave.ALEATORIA)
        )
    }

    @ParameterizedTest
    @MethodSource("chavePixValida")
    fun `validacao da chave pix não deve retornar constraint violations`(tipoChaveRequest: TipoChaveRequest, valorChave: String?) {
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId("234d861f-9b3d-4259-976b-b79750199ed5")
            .setTipoChave(tipoChaveRequest)
            .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
            .setValorChave(valorChave).build()
        val validatedProxy: ChavePixValidatedProxy = request.toValidatedProxy()
        assertEquals(TipoConta.CORRENTE, validatedProxy.tipoConta)
        assertNotNull(validatedProxy)
        val constraintViolations = validator.validate(validatedProxy)
        constraintViolations.toString().let(::println)
        assertEquals(0, constraintViolations.size)
    }

    @ParameterizedTest
    @MethodSource("chavePixInvalida")
    fun `validacao chave pix deve falhar para par tipo-valor invalidos`(tipoChaveRequest: TipoChaveRequest, valorChave: String?, tipoChave: TipoChave) {
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId("234d861f-9b3d-4259-976b-b79750199ed5")
            .setTipoConta(TipoContaRequest.TIPO_CONTA_POUPANCA)
            .setTipoChave(tipoChaveRequest)
            .setValorChave(valorChave).build()
        val validatedProxy: ChavePixValidatedProxy = request.toValidatedProxy()
        assertEquals(TipoConta.POUPANCA, validatedProxy.tipoConta)
        assertEquals(tipoChave, validatedProxy.tipoChave)
        val constraintViolations = validator.validate(validatedProxy)
        assertEquals(1, constraintViolations.size)
        assertTrue(constraintViolations.any {
            it.message.equals("Chave Pix inválida")
        })
    }

    @Test
    fun `validacao da chave pix deve retornar constraint violations`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId("1234")
            .setTipoChave(TipoChaveRequest.TIPO_CHAVE_UNSPECIFIED)
            .setTipoConta(TipoContaRequest.TIPO_CONTA_UNSPECIFIED)
            .setValorChave("").build()
        val validatedProxy: ChavePixValidatedProxy = request.toValidatedProxy()
        assertEquals(null, validatedProxy.tipoChave)
        assertEquals(null, validatedProxy.tipoConta)
        val constraintViolations = validator.validate(validatedProxy)
        assertEquals(4, constraintViolations.size)
        with(constraintViolations) {
            val clienteIdFormatoErradoQntd = filter {
                it.propertyPath.toString() == "clienteId" && it.message.equals("Campo deve estar no formato de UUID")
            }.size
            val tipoContaNuloQntd = filter {
                it.propertyPath.toString() == "tipoConta" && it.message.equals("não deve ser nulo")
            }.size
            val tipoChaveNuloQntd = filter {
                it.propertyPath.toString() == "tipoChave" && it.message.equals("não deve ser nulo")
            }.size
            val chavePixInvalidaQntd = filter {
                it.message.equals("Chave Pix inválida")
            }.size
            assertEquals(1, clienteIdFormatoErradoQntd)
            assertEquals(1, tipoContaNuloQntd)
            assertEquals(1, tipoChaveNuloQntd)
            assertEquals(1, chavePixInvalidaQntd)
        }
    }
}