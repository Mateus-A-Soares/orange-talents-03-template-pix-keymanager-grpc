package br.com.zupacademy.chave

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRespository : JpaRepository<ChavePix, UUID>{

    fun existsByChave(chave: String): Boolean
}
