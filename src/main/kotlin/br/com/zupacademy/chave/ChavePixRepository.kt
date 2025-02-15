package br.com.zupacademy.chave

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID>{

    fun existsByChave(chave: String): Boolean

    fun findByIdAndClienteId(chaveUuid: UUID?, clienteUuid: UUID?): ChavePix?

    fun findByChave(chave: String): ChavePix?

    fun findByClienteId(clienteId: UUID?): List<ChavePix>
}
