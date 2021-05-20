package br.com.zupacademy.chave

import br.com.zupacademy.chave.conta.Conta
import io.micronaut.data.annotation.DateCreated
import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotBlank
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotBlank
    @Column(nullable = false)
    val chave: String,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @Embedded
    val conta: Conta
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    @DateCreated
    val persistenceTimestamp: Instant? = null
}
