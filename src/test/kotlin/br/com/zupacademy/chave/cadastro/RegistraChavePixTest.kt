package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.*
import br.com.zupacademy.ChavePixServiceGrpc.ChavePixServiceBlockingStub
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRespository
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse
import br.com.zupacademy.itauerp.InstituicaoResponse
import br.com.zupacademy.itauerp.ItauErpClient
import br.com.zupacademy.itauerp.TitularResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChavePixTest(
    val repository: ChavePixRespository,
    val grpcClient: ChavePixServiceBlockingStub
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @Inject
    lateinit var itauErpClient: ItauErpClient

    @MockBean(ItauErpClient::class)
    fun itauClient(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ChavePixServiceBlockingStub {
            return ChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun deveRegistrarChave() {
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = CLIENTE_ID.toString(),
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        )
            .thenReturn(itauClientResponse())

        val response: ChavePixCadastradaResponse = grpcClient.registra(
            CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoChave(TipoChaveRequest.TIPO_CHAVE_EMAIL)
                .setValorChave("teste@email.com")
                .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
                .build()
        )

        with(response) {
            assertNotNull(chavePixId)
        }
    }

    @Test
    fun naoDeveRegistrarChaveComErrosDeValidacao() {
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = CLIENTE_ID.toString(),
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        )
            .thenReturn(itauClientResponse())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                CadastraChavePixRequest.newBuilder()
                    .setClienteId("")
                    .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
                    .setTipoChave(TipoChaveRequest.TIPO_CHAVE_CPF)
                    .setValorChave("teste@email.com")
                    .build()
            )
        }


        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
//            exception.
        }
    }

    @Test
    fun naoDeveCadastrarChaveComContaInexistente() {
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = CLIENTE_ID.toString(),
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        )
            .thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                CadastraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChaveRequest.TIPO_CHAVE_EMAIL)
                    .setValorChave("teste@email.com")
                    .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Conta não foi encontrada", status.description)
        }
    }

    @Test
    fun naoDeveCadastrarChaveComChaveExistente() {
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = CLIENTE_ID.toString(),
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        )
            .thenReturn(itauClientResponse())

        repository.save(
            ChavePix(
                tipoChave = TipoChave.EMAIL,
                chave = "teste@email.com",
                clienteId = CLIENTE_ID,
                conta = itauClientResponse()!!.body().toModel()
            )
        )

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                CadastraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoChave(TipoChaveRequest.TIPO_CHAVE_EMAIL)
                    .setValorChave("teste@email.com")
                    .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("chave já tem o valor cadastrado", status.description)
        }
    }

    private fun itauClientResponse(): HttpResponse<BuscarContaTipoItauErpResponse>? {
        return HttpResponse.ok(
            BuscarContaTipoItauErpResponse(
                tipo = TipoConta.CORRENTE.itauErpParameterName,
                agencia = "",
                numero = "",
                instituicao = InstituicaoResponse(nome = "", ispb = ""),
                titular = TitularResponse(id = CLIENTE_ID.toString(), nome = "", cpf = "")
            )
        )
    }
}