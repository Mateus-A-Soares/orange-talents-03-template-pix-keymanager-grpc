package br.com.zupacademy.chave

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator

enum class TipoChave(val validate: (value: String?) -> Boolean) {
    CPF(validate@{ value ->
        value ?: return@validate false
        value.matches("\"[0-9]{11}".toRegex())
    }),
    TELEFONE(validate@{ value ->
        value ?: return@validate false
        value.matches("\\+[1-9][0-9]\\d{1,14}".toRegex())
    }),
    EMAIL(validate@{ value ->
        value ?: return@validate false
        EmailValidator().run {
            initialize(null)
            return@validate isValid(value, null)
        }
    }),
    ALEATORIA(validate = { value ->
        value.isNullOrBlank()
    });
}