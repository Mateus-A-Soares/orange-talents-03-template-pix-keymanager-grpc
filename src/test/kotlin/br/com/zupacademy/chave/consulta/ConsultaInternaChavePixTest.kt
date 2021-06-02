package br.com.zupacademy.chave.consulta

import br.com.zupacademy.ConsultaChavePixServiceGrpc
import br.com.zupacademy.ConsultaChavePixServiceGrpc.ConsultaChavePixServiceBlockingStub
import br.com.zupacademy.ConsultaInternaChavePixRequest
import br.com.zupacademy.bcb.*
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRepository
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.chave.conta.Conta
import br.com.zupacademy.chave.conta.ItauBank
import br.com.zupacademy.instituicoes.Instituicao
import br.com.zupacademy.instituicoes.InstituicaoRepository
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
import org.junit.jupiter.api.Assertions.assertEquals
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
class ConsultaInternaChavePixTest(
    private val repository: ChavePixRepository,
    @InjectMocks private val grpcClient: ConsultaChavePixServiceBlockingStub
) {

    private val clienteNome = "José"
    private val clienteCpf = "11111111111"

    @Inject
    lateinit var itauClient: ItauErpClient

    @Inject
    lateinit var bcbClient: BcbClient

    @Inject
    lateinit var instituicaoRepository: InstituicaoRepository


    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }

    @MockBean(ItauErpClient::class)
    fun itauErpClient(): ItauErpClient {
        return Mockito.mock(ItauErpClient::class.java)
    }

    @MockBean(InstituicaoRepository::class)
    fun instituicaoRepository(): InstituicaoRepository {
        return Mockito.mock(InstituicaoRepository::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ConsultaChavePixServiceBlockingStub {
            return ConsultaChavePixServiceGrpc.newBlockingStub(channel)
        }
    }

    @Test
    fun `deve encontar chave pix por chave existente na base de dados`() {
        val chavePix = ChavePix(
            clienteId = UUID.randomUUID(),
            tipoChave = TipoChave.ALEATORIA,
            chave = UUID.randomUUID().toString(),
            conta = Conta(instituicao = "ITAÚ", agencia = "111111", numero = "0101", tipo = TipoConta.POUPANCA)
        )
        repository.save(chavePix)
        val itauClientResponse = itauClientResponse(chavePix = chavePix, clienteNome = clienteNome, clienteCpf = clienteCpf)
        `when`(
            itauClient.buscaPorContaTipo(
                clienteId = chavePix.clienteId.toString(),
                tipoConta = chavePix.conta.tipo.itauErpParameterName
            )
        ).thenReturn(itauClientResponse)

        val response = grpcClient.consultaInterna(
            ConsultaInternaChavePixRequest.newBuilder().setChavePix(chavePix.chave).build()
        )
        
        with(chavePix) {
            assertEquals(id, UUID.fromString(response.chavePixId))
            assertEquals(clienteId, UUID.fromString(response.clienteId))
            assertEquals(chave, response.valorChave)
            assertEquals(conta.numero, response.conta.numero)
        }
        repository.deleteAll()
    }

    @Test
    fun `deve encontar chave pix por chave existente somente no bcb`() {
        val key = UUID.randomUUID().toString()
        val instituicao = Instituicao(
            ispb = "208",
            nomeReduzido = "BRB - BCO DE BRASILIA S.A.",
            nomeExtenso = "BRB - BANCO DE BRASILIA S.A."
        )
        val chaveBcb = ChavePixBcbDetailsResponse(
            keyType = KeyTypeBcb.RANDOM,
            key = key,
            bankAccount = BankAccountBcbResponse(
                participant = instituicao.ispb,
                branch = "0001",
                accountNumber = "11111-11",
                accountType = AccountTypeBcb.CACC
            ),
            owner = OwnerBcbResponse(
                type = TypeBcb.NATURAL_PERSON,
                name = "Maria",
                taxIdNumber = "00000000000"
            ),
            createdAt = LocalDateTime.now()
        )
        `when`(
            bcbClient.buscaChave(key)
        ).thenReturn(HttpResponse.ok(chaveBcb))
        `when`(
            instituicaoRepository.findByIspb("208")
        ).thenReturn(
            instituicao
        )

        val response = grpcClient.consultaInterna(
            ConsultaInternaChavePixRequest.newBuilder().setChavePix(key).build()
        )

        assertEquals(chaveBcb.key, response.valorChave)
        assertEquals(chaveBcb.bankAccount.accountNumber, response.conta.numero)
        assertEquals(instituicao.nomeReduzido, response.conta.instituicao)
    }

    @Test
    fun `nao deve encontar chave pix nao cadastrada em nenhuma base`() {
        val key = UUID.randomUUID().toString()
        `when`(
            bcbClient.buscaChave(key)
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultaInterna(
                ConsultaInternaChavePixRequest.newBuilder().setChavePix(key).build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
        }
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