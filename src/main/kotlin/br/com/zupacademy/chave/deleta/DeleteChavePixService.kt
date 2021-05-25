package br.com.zupacademy.chave.deleta

import br.com.zupacademy.chave.ChavePixRespository
import br.com.zupacademy.shared.constraints.ValidUUID
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class DeleteChavePixService(@Inject val repository: ChavePixRespository) {

    @Transactional
    fun deleta(
        @NotBlank @ValidUUID(message = "Id do cliente não pode estar vazio") clienteId: String?,
        @NotBlank @ValidUUID(message = "Id da chave não pode estar vazio") chaveId: String?
    ) {
        val clienteUuid = UUID.fromString(clienteId)
        val chaveUuid = UUID.fromString(chaveId)
        val chave = repository.findByIdAndClienteId(chaveUuid, clienteUuid)?: throw FieldNotFoundException(field = "chave", message = "Chave não está na lista de chaves do cliente")
        repository.delete(chave)
    }
}