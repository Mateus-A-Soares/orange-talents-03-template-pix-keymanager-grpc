package br.com.zupacademy.chave.consulta

import br.com.zupacademy.ConsultaChavePixRequest
import br.com.zupacademy.ConsultaChavePixServiceGrpc
import br.com.zupacademy.ConsultaChavePixServiceGrpc.ConsultaChavePixServiceBlockingStub
import br.com.zupacademy.bcb.*
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRepository
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.chave.conta.Conta
import br.com.zupacademy.chave.conta.ItauBank
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
internal class ConsultaChavePixTest(
    private val repository: ChavePixRepository,
    @InjectMocks private val grpcClient: ConsultaChavePixServiceBlockingStub
) {


    private val clienteNome = "José"
    private val clienteCpf = "11111111111"
    private lateinit var chave: ChavePix
    private lateinit var itauClientResponse: HttpResponse<BuscarContaTipoItauErpResponse>
    private lateinit var bcbClientResponse: HttpResponse<ChavePixBcbDetailsResponse>

    @Inject
    lateinit var itauClient: ItauErpClient

    @Inject
    lateinit var bcbClient: BcbClient


    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @MockBean(ItauErpClient::class)
    fun itauErpClient(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultaChavePixServiceBlockingStub {
            return ConsultaChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @BeforeEach
    fun setup() {
        chave = ChavePix(
            clienteId = UUID.randomUUID(),
            tipoChave = TipoChave.ALEATORIA,
            chave = UUID.randomUUID().toString(),
            conta = Conta(instituicao = "ITAÚ", agencia = "111111", numero = "0101", tipo = TipoConta.POUPANCA)
        )
        itauClientResponse = itauClientResponse(chavePix = chave, clienteNome = clienteNome, clienteCpf = clienteCpf)
        bcbClientResponse = bcbClientResponse(
            chavePix = chave, owner = with(itauClientResponse.body.get().titular) {
                OwnerBcbResponse(
                    type = TypeBcb.NATURAL_PERSON,
                    name = nome,
                    taxIdNumber = cpf
                )
            }, bankAccount = with(chave.conta) {
                BankAccountBcbResponse(
                    participant = ItauBank.NOME,
                    branch = agencia,
                    accountNumber = numero,
                    accountType = AccountTypeBcb.of(tipo)
                )
            }
        )
        repository.save(chave)
    }

    @Test
    fun `deve encontar chave pix por id e cliente validos`() {
        `when`(
            bcbClient.buscaChave(chave.chave)
        ).thenReturn(bcbClientResponse)
        `when`(
            itauClient.buscaContaPorTipo(
                clienteId = chave.clienteId.toString(),
                tipoConta = chave.conta.tipo.itauErpParameterName
            )
        ).thenReturn(itauClientResponse)

        val response = grpcClient.consulta(
            ConsultaChavePixRequest.newBuilder().setChavePixId(chave.id.toString())
                .setClienteId(chave.clienteId.toString()).build()
        )

        assertEquals(chave.id, UUID.fromString(response.chavePixId))
        assertEquals(chave.clienteId, UUID.fromString(response.clienteId))
        assertEquals(chave.chave, response.valorChave)
        assertEquals(chave.conta.numero, response.conta.numero)
    }

    @Test
    fun `nao deve encontar chave pix quando parametros forem invalidos`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder().setChavePixId("")
                    .setClienteId("").build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve encontar chave pix nao existir no banco de dados`() {
        val outraChavePix = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder().setChavePixId(outraChavePix)
                    .setClienteId(chave.clienteId.toString()).build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não está na lista de chaves do cliente", status.description)
        }
    }

    @Test
    fun `nao deve encontar chave pix quando chave nao pertencer ao cliente`() {
        val outroClienteId = UUID.randomUUID().toString()
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder().setChavePixId(chave.id.toString())
                    .setClienteId(outroClienteId).build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não está na lista de chaves do cliente", status.description)
        }
    }

    @Test
    fun `nao deve encontar chave pix quando chave nao for encontrada no servico BCB`() {
        `when`(
            bcbClient.buscaChave(chave.chave)
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder().setChavePixId(chave.id.toString())
                    .setClienteId(chave.clienteId.toString()).build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não está na lista de chaves do cliente", status.description)
        }
    }

    @Test
    fun `nao deve encontar chave pix quando conta nao for encontrada no servico ItauERP`() {
        `when`(
            bcbClient.buscaChave(chave.chave)
        ).thenReturn(bcbClientResponse)
        `when`(
            itauClient.buscaContaPorTipo(
                clienteId = chave.clienteId.toString(),
                tipoConta = chave.conta.tipo.itauErpParameterName
            )
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChavePixRequest.newBuilder().setChavePixId(chave.id.toString())
                    .setClienteId(chave.clienteId.toString()).build()
            )
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Conta não foi encontrado", status.description)
        }
    }

    @AfterEach
    fun teardown() {
        repository.deleteAll()
    }

    private fun itauClientResponse(
        chavePix: ChavePix,
        clienteNome: String,
        clienteCpf: String
    ): HttpResponse<BuscarContaTipoItauErpResponse> {
        return with(chavePix) {
            HttpResponse.ok(
                BuscarContaTipoItauErpResponse(
                    tipo = conta.tipo.itauErpParameterName,
                    agencia = conta.agencia,
                    numero = conta.numero,
                    instituicao = InstituicaoResponse(nome = ItauBank.NOME, ispb = ItauBank.ISPB),
                    titular = TitularResponse(id = clienteId.toString(), nome = clienteNome, cpf = clienteCpf)
                )
            )
        }
    }

    private fun bcbClientResponse(
        chavePix: ChavePix,
        bankAccount: BankAccountBcbResponse,
        owner: OwnerBcbResponse
    ): HttpResponse<ChavePixBcbDetailsResponse> {

        return with(chavePix) {
            HttpResponse.ok(
                ChavePixBcbDetailsResponse(
                    keyType = KeyTypeBcb.of(tipoChave),
                    key = chave,
                    bankAccount = bankAccount,
                    owner = owner,
                    createdAt = LocalDateTime.now()
                )
            )
        }
    }
}