package br.com.zupacademy.chave.deleta

import br.com.zupacademy.DeletaChavePixRequest
import br.com.zupacademy.DeletaChavePixServiceGrpc
import br.com.zupacademy.DeletaChavePixServiceGrpc.DeletaChavePixServiceBlockingStub
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRespository
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.chave.conta.Conta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class DeletaChavePixTest(
    val repository: ChavePixRespository,
    val grpcClient: DeletaChavePixServiceBlockingStub
) {

    lateinit var CHAVE: ChavePix

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DeletaChavePixServiceBlockingStub {
            return DeletaChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @BeforeEach
    fun setup() {
        CHAVE = ChavePix(
            clienteId = UUID.randomUUID(),
            tipoChave = TipoChave.ALEATORIA,
            chave = UUID.randomUUID().toString(),
            conta = Conta(instituicao = "ITAÙ", agencia = "111111", numero = "0101", tipo = TipoConta.POUPANCA)
        )
        repository.save(CHAVE)
    }

    @Test
    fun deveDeletarChavePixExistente() {
        grpcClient.deleta(
            DeletaChavePixRequest.newBuilder().setChavePixId(CHAVE.id.toString())
                .setClienteId(CHAVE.clienteId.toString()).build()
        )
        assertFalse(repository.existsById(CHAVE.id))
    }

    @Test
    fun deveRetornarNotFoundParaChavePixInexistente() {
        val pixIdInexistente = UUID.randomUUID()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder().setChavePixId(pixIdInexistente.toString())
                    .setClienteId(CHAVE.clienteId.toString()).build()
            )
        }
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não está na lista de chaves do cliente", status.description)
        }
    }

    @Test
    fun naoDeveDeletarChavePixDeOutroCliente() {
        val idOutroCliente = UUID.randomUUID()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder().setChavePixId(CHAVE.id.toString())
                    .setClienteId(idOutroCliente.toString()).build()
            )
        }
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não está na lista de chaves do cliente", status.description)
            assertTrue(repository.existsById(CHAVE.id))
        }
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }
}