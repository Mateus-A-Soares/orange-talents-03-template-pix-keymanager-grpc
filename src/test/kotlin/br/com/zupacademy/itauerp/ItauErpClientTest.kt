package br.com.zupacademy.itauerp

import br.com.zupacademy.chave.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ItauErpClientTest(@Inject val itauErpClient: ItauErpClient) {

    @Test
    fun deveRetornarHttpResponse() {
        val buscaPorContaTipo: HttpResponse<BuscarContaTipoItauErpResponse> = itauErpClient.buscaContaPorTipo(
            clienteId = "9478460b-9133-4546-bacc-43cce3e43971",
            tipoConta = TipoConta.CORRENTE.itauErpParameterName
        )
        assertNotNull(buscaPorContaTipo)
        buscaPorContaTipo.toString().let(::println)
    }

    @Test
    fun naoDeveAceitarDadosInvalidos() {
        val exception = assertThrows<HttpClientResponseException> {
            itauErpClient.buscaContaPorTipo(
                clienteId = "",
                tipoConta = ""
            )
        }
        with(exception) {
            assertEquals(HttpStatus.BAD_REQUEST.code, status.code)
        }
    }

    @Test
    fun responseDeveRetornarContaPoupanca() {
        val titular = TitularResponse(id= "d431bc13-b9e0-4aca-8fdf-cefac55634a2", nome = "Mateus Almeida", cpf = "11111111111")
        val instituicao = InstituicaoResponse(nome= "ITAÚ UNIBANCO", ispb = "60701190")
        val response = BuscarContaTipoItauErpResponse(tipo= TipoConta.POUPANCA.itauErpParameterName, agencia = "0101", numero = "111111", instituicao= instituicao, titular = titular)
        val model = response.toModel()
        assertNotNull(model)
        assertEquals(TipoConta.POUPANCA, model.tipo)
        assertEquals(response.agencia, model.agencia)
        assertEquals(instituicao.nome, model.instituicao)
        assertEquals(response.numero, model.numero)
    }

    @Test
    fun responseDeveRetornarContaCorrente() {
        val titular = TitularResponse(id= "d431bc13-b9e0-4aca-8fdf-cefac55634a2", nome = "Mateus Almeida", cpf = "11111111111")
        val instituicao = InstituicaoResponse(nome= "ITAÚ UNIBANCO", ispb = "60701190")
        val response = BuscarContaTipoItauErpResponse(tipo= TipoConta.POUPANCA.itauErpParameterName, agencia = "0101", numero = "111111", instituicao= instituicao, titular = titular)
        val model = response.toModel()
        assertNotNull(model)
        assertEquals(TipoConta.POUPANCA, model.tipo)
        assertEquals(response.agencia, model.agencia)
        assertEquals(instituicao.nome, model.instituicao)
        assertEquals(response.numero, model.numero)
    }
}