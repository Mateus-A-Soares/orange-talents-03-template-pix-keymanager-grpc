package br.com.zupacademy.instituicoes

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface InstituicaoRepository : JpaRepository<Instituicao, String> {

    fun findByIspb(ispb: String) : Instituicao?
}
