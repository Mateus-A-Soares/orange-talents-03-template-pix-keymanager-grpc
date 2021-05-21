package br.com.zupacademy.shared.constraints

import br.com.zupacademy.chave.ChavePixRespository
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = [ChavePixUniqueValidator::class])
annotation class ChavePixUnique(val message: String = "Valor para o campo já está cadastastrado",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ChavePixUniqueValidator(@Inject private val repository: ChavePixRespository) : ConstraintValidator<ChavePixUnique, String>{

    override fun isValid(
        chavePix: String,
        annotationMetadata: AnnotationValue<ChavePixUnique>,
        context: ConstraintValidatorContext
    ): Boolean {
        return !repository.existsByChave(chavePix)
    }
}
