package br.com.zupacademy.chave.deleta

import br.com.zupacademy.DeletaChavePixRequest
import br.com.zupacademy.DeletaChavePixServiceGrpc
import br.com.zupacademy.DeletaChavePixServiceGrpc.DeletaChavePixServiceBlockingStub
import br.com.zupacademy.bcb.BcbClient
import br.com.zupacademy.bcb.DeletaChavePixBcbRequest
import br.com.zupacademy.bcb.DeletaChavepPixBcbResponse
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRepository
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.chave.conta.Conta
import br.com.zupacademy.chave.conta.ItauBank
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class DeletaChavePixTest(
    val repository: ChavePixRepository,
    @InjectMocks val grpcClient: DeletaChavePixServiceBlockingStub
) {

    private lateinit var CHAVE: ChavePix

    @Inject
    lateinit var bcbClient: BcbClient

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

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
    fun `deve deletar chave pix existente`() {
        `when`(bcbClient.deletaChave(CHAVE.chave, DeletaChavePixBcbRequest(CHAVE.chave)))
            .thenReturn(
                HttpResponse.ok(
                    DeletaChavepPixBcbResponse(
                        key = CHAVE.chave,
                        deletedAt = LocalDateTime.now(),
                        participant = ItauBank.ISPB
                    )
                )
            )
        grpcClient.deleta(
            DeletaChavePixRequest.newBuilder().setChavePixId(CHAVE.id.toString())
                .setClienteId(CHAVE.clienteId.toString()).build()
        )
        assertFalse(repository.existsById(CHAVE.id))
    }

    @Test
    fun `deve retornar not found para chave pix nao cadastrada no banco de dados`() {
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
    fun `nao deve deletar chave pix de outro cliente`() {
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

    @Test
    fun `deve excluir chave no banco e retornar not found para chave pix nao cadastrada no bcb`() {
        `when`(bcbClient.deletaChave(CHAVE.chave, DeletaChavePixBcbRequest(CHAVE.chave)))
            .thenReturn(
                HttpResponse.notFound()
            )

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder().setChavePixId(CHAVE.id.toString())
                    .setClienteId(CHAVE.clienteId.toString()).build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não está na lista de chaves do cliente", status.description)
        }

        assertFalse(repository.existsByChave(CHAVE.chave))
    }

    @Test
    fun `nao deve excluir chave quando bcb retornar erro inesperado`(){
        `when`(bcbClient.deletaChave(CHAVE.chave, DeletaChavePixBcbRequest(CHAVE.chave)))
            .thenReturn((HttpResponse.unauthorized()))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChavePixRequest.newBuilder().setChavePixId(CHAVE.id.toString())
                    .setClienteId(CHAVE.clienteId.toString()).build()
            )
        }

        assertEquals(Status.UNKNOWN.code, exception.status.code)
        assertTrue(repository.existsByChave(CHAVE.chave))
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }
}