package br.com.zupacademy.instituicoes

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
class Instituicao(
    @Id
    @Size(min = 8, max = 8)
    val ispb: String,

    @NotBlank
    @Column(nullable = false)
    val nomeReduzido: String,

    @NotBlank
    @Column(nullable = false)
    val nomeExtenso: String
)