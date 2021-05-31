package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.*
import br.com.zupacademy.CadastraChavePixServiceGrpc.CadastraChavePixServiceBlockingStub
import br.com.zupacademy.bcb.*
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRepository
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
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
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChavePixTest(
    val repository: ChavePixRepository,
    @InjectMocks val grpcClient: CadastraChavePixServiceBlockingStub
) {

    private companion object {
        val CLIENTE_ID = UUID.randomUUID()

        @JvmStatic
        fun chavePixValida() = Stream.of(
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_CPF, "11111111111"),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_EMAIL, "teste@email.com"),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_TELEFONE, "+551111111111"),
            Arguments.of(TipoChaveRequest.TIPO_CHAVE_ALEATORIA, "")
        )
    }

    @Inject
    lateinit var itauErpClient: ItauErpClient

    @Inject
    lateinit var bcbClient: BcbClient

    @MockBean(ItauErpClient::class)
    fun itauClient(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CadastraChavePixServiceBlockingStub {
            return CadastraChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @ParameterizedTest
    @MethodSource("chavePixValida")
    fun `deve registrar chave`(tipoChaveRequest: TipoChaveRequest, valorChave: String) {
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(tipoChaveRequest)
            .setValorChave(valorChave)
            .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
            .build()
        val itauClientResponse = itauClientResponse()
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = request.clienteId,
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        ).thenReturn(itauClientResponse)
        `when`(
            bcbClient.cadastraChave(
                request = CadastraChavePixBcbRequest(
                    keyType = KeyTypeBcb.of(request.tipoChave.toModel()!!),
                    key = request.valorChave,
                    bankAccount = BankAccountBcbRequest.of(itauClientResponse!!.body().toModel()),
                    owner = OwnerBcbRequest.of(itauClientResponse!!.body().titular),
                )
            )
        ).thenReturn(bcbClientResponse(request, itauClientResponse.body()))

        val response: ChavePixCadastradaResponse = grpcClient.registra(request)

        with(response) {
            assertNotNull(chavePixId)
        }
    }

    @Test
    fun `nao deve registrar chave com erros de validacao`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId("")
            .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
            .setTipoChave(TipoChaveRequest.TIPO_CHAVE_CPF)
            .setValorChave("teste@email.com")
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave com conta inexistente`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(TipoChaveRequest.TIPO_CHAVE_EMAIL)
            .setValorChave("teste@email.com")
            .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
            .build()
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = request.clienteId,
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Conta não foi encontrada", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave ja cadastrada no banco`() {
        val itauResponse = itauClientResponse()
        val chavePix = repository.save(
            ChavePix(
                tipoChave = TipoChave.EMAIL,
                chave = "teste@email.com",
                clienteId = CLIENTE_ID,
                conta = itauResponse!!.body().toModel()
            )
        )
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(chavePix.clienteId.toString())
            .setTipoChave(TipoChaveRequest.TIPO_CHAVE_EMAIL)
            .setValorChave(chavePix.chave)
            .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
            .build()
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = CLIENTE_ID.toString(),
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        ).thenReturn(itauResponse)
        `when`(
            bcbClient.cadastraChave(
                request = CadastraChavePixBcbRequest(
                    keyType = KeyTypeBcb.of(request.tipoChave.toModel()!!),
                    key = request.valorChave,
                    bankAccount = BankAccountBcbRequest.of(itauResponse!!.body().toModel()),
                    owner = OwnerBcbRequest.of(itauResponse!!.body().titular),
                )
            )
        ).thenReturn(bcbClientResponse(request, itauResponse.body()))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("chave já tem o valor cadastrado", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave ja cadastrada no servico bcb`() {
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoChave(TipoChaveRequest.TIPO_CHAVE_EMAIL)
            .setValorChave("chavecadastrada@bcb")
            .setTipoConta(TipoContaRequest.TIPO_CONTA_CORRENTE)
            .build()
        val itauResponse = itauClientResponse()
        val bcbResponse = HttpResponse.unprocessableEntity<Problem>().body(
            Problem(
                type = "UNPROCESSABLE_ENTITY",
                status = 422,
                title = "Unprocessable Entity",
                detail = "The informed Pix key exists already",
                violations = null
            )
        )
        `when`(
            itauErpClient.buscaPorContaTipo(
                clienteId = CLIENTE_ID.toString(),
                tipoConta = TipoConta.CORRENTE.itauErpParameterName
            )
        ).thenReturn(itauResponse)
        `when`(
            bcbClient.cadastraChave(
                request = CadastraChavePixBcbRequest(
                    keyType = KeyTypeBcb.of(request.tipoChave.toModel()!!),
                    key = request.valorChave,
                    bankAccount = BankAccountBcbRequest.of(itauResponse!!.body().toModel()),
                    owner = OwnerBcbRequest.of(itauResponse!!.body().titular),
                )
            )
        ).thenThrow(HttpClientResponseException("", bcbResponse))

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.registra(request)
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals(bcbResponse.body.get().detail, status.description)
        }
    }

    private fun itauClientResponse(): HttpResponse<BuscarContaTipoItauErpResponse> {
        return HttpResponse.ok(
            BuscarContaTipoItauErpResponse(
                tipo = TipoConta.CORRENTE.itauErpParameterName,
                agencia = "",
                numero = "",
                instituicao = InstituicaoResponse(nome = "", ispb = ItauBank.ISPB),
                titular = TitularResponse(id = CLIENTE_ID.toString(), nome = "", cpf = "")
            )
        )
    }

    private fun bcbClientResponse(
        chaveRequest: CadastraChavePixRequest,
        itauErpResponse: BuscarContaTipoItauErpResponse
    ): HttpResponse<ChavePixBcbDetailsResponse> {
        val conta = itauErpResponse.toModel()
        val titular = itauErpResponse.titular
        val keyType = KeyTypeBcb.of(chaveRequest.tipoChave.toModel()!!)
        val key = if (keyType != KeyTypeBcb.RANDOM) chaveRequest.valorChave else UUID.randomUUID().toString()
        return HttpResponse.ok(
            ChavePixBcbDetailsResponse(
                keyType = keyType,
                key = key,
                bankAccount = BankAccountBcbResponse(
                    accountNumber = conta.numero,
                    accountType = AccountTypeBcb.of(conta.tipo),
                    branch = conta.agencia,
                    participant = ItauBank.ISPB
                ),
                owner = OwnerBcbResponse(
                    type = TypeBcb.NATURAL_PERSON,
                    name = titular.nome,
                    taxIdNumber = titular.cpf
                ),
                createdAt = LocalDateTime.now()
            )
        )
    }
}