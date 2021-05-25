package br.com.zupacademy.chave

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.ChavePixCadastradaResponse
import br.com.zupacademy.TipoChaveRequest
import br.com.zupacademy.TipoContaRequest
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse
import br.com.zupacademy.itauerp.InstituicaoResponse
import br.com.zupacademy.itauerp.ItauErpClient
import br.com.zupacademy.itauerp.TitularResponse
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import br.com.zupacademy.shared.exceptions.UniqueFieldAlreadyExistsException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@MicronautTest(transactional = false)
internal class ChavePixServiceTest() {

    private lateinit var chavePixValidatedProxy: ChavePixValidatedProxy

    @MockBean(ItauErpClient::class)
    fun itauClient(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @MockBean(ChavePixRespository::class)
    fun repository(): ChavePixRespository {
        return Mockito.mock(ChavePixRespository::class.java)
    }

    @Test
    fun deveEfetuarFuncaoCadastra() {
        val repository = repository()
        val itauClient = itauClient()
        val chavePixService = ChavePixService(repository = repository, itauClient = itauClient)
        chavePixValidatedProxy = chavePixValidatedProxy()
        `when`(
            itauClient.buscaPorContaTipo(
                clienteId = chavePixValidatedProxy.clienteId.toString(),
                tipoConta = chavePixValidatedProxy.tipoConta!!.itauErpParameterName
            )
        ).thenReturn(itauClientResponse())
        `when`(
            repository.existsByChave(chavePixValidatedProxy.chave.toString())
        ).thenReturn(false)
        val validatedProxy = chavePixValidatedProxy()
        val response: ChavePixCadastradaResponse = chavePixService.cadastra(validatedProxy)
        assertNotNull(response.chavePixId)
    }

    @Test
    fun deveDispararFieldNotFoundException() {
        val repository = repository()
        val itauClient = itauClient()
        val chavePixService = ChavePixService(repository = repository, itauClient = itauClient)
        chavePixValidatedProxy = chavePixValidatedProxy()
        `when`(
            itauClient.buscaPorContaTipo(
                clienteId = chavePixValidatedProxy.clienteId.toString(),
                tipoConta = chavePixValidatedProxy.tipoConta!!.itauErpParameterName
            )
        ).thenReturn(HttpResponse.notFound())
        val validatedProxy = chavePixValidatedProxy()
        val exception = assertThrows<FieldNotFoundException> { chavePixService.cadastra(validatedProxy) }
        assertEquals("Conta", exception.field)
        assertEquals("Conta não foi encontrada", exception.message)
    }

    @Test
    fun deveDispararUniqueFieldAlreadyExistsException() {
        val repository = repository()
        val itauClient = itauClient()
        val chavePixService = ChavePixService(repository = repository, itauClient = itauClient)
        chavePixValidatedProxy = chavePixValidatedProxy()
        `when`(
            itauClient.buscaPorContaTipo(
                clienteId = chavePixValidatedProxy.clienteId.toString(),
                tipoConta = chavePixValidatedProxy.tipoConta!!.itauErpParameterName
            )
        ).thenReturn(itauClientResponse())
        `when`(
            repository.existsByChave(chavePixValidatedProxy.chave.toString())
        ).thenReturn(true)
        val validatedProxy = chavePixValidatedProxy()
        val exception = assertThrows<UniqueFieldAlreadyExistsException> { chavePixService.cadastra(validatedProxy) }
        assertEquals("chave", exception.field)
    }

    private fun chavePixValidatedProxy(): ChavePixValidatedProxy {
        return CadastraChavePixRequest.newBuilder()
            .setClienteId("234d861f-9b3d-4259-976b-b79750199ed5")
            .setTipoChave(TipoChaveRequest.TIPO_CHAVE_EMAIL)
            .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
            .setValorChave("a@a").build().toValidatedProxy()
    }

    private fun itauClientResponse(): HttpResponse<BuscarContaTipoItauErpResponse>? {
        return HttpResponse.ok(
            BuscarContaTipoItauErpResponse(
                tipo = chavePixValidatedProxy.tipoConta!!.itauErpParameterName,
                agencia = "0101",
                numero = "111111",
                instituicao = InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A.", ispb = "60701190"),
                titular = TitularResponse(id = chavePixValidatedProxy.clienteId.toString(), nome = "", cpf = "")
            )
        )
    }
}