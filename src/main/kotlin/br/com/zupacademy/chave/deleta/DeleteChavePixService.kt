package br.com.zupacademy.chave.deleta

import br.com.zupacademy.bcb.BcbClient
import br.com.zupacademy.bcb.DeletaChavePixBcbRequest
import br.com.zupacademy.chave.ChavePixRespository
import br.com.zupacademy.shared.constraints.ValidUUID
import br.com.zupacademy.shared.exceptions.ApiException
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import com.google.rpc.Code
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class DeleteChavePixService(@Inject val repository: ChavePixRespository, @Inject val bcbClient: BcbClient) {

    @Transactional
    fun deleta(
        @NotBlank @ValidUUID(message = "Id do cliente não pode estar vazio") clienteId: String?,
        @NotBlank @ValidUUID(message = "Id da chave não pode estar vazio") chave: String?
    ) {
        val clienteUuid = UUID.fromString(clienteId)
        val chaveUuid = UUID.fromString(chave)
        val chavePix = repository.findByIdAndClienteId(chaveUuid, clienteUuid) ?: throw FieldNotFoundException(
            field = "chave",
            message = "Chave não está na lista de chaves do cliente",
            rpcCode = Code.NOT_FOUND
        )
        val response =
            bcbClient.deletaChave(chave = chavePix.chave, request = DeletaChavePixBcbRequest(key = chavePix.chave))
        if (response.status != HttpStatus.OK) throw ApiException()
        repository.delete(chavePix)
    }
}