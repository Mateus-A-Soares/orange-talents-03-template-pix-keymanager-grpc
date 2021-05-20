package br.com.zupacademy.chave

enum class TipoConta(val itauErpParameterName: String) {
    POUPANCA(itauErpParameterName = "CONTA_POUPANCA"), CORRENTE(itauErpParameterName = "CONTA_CORRENTE");

    companion object {
        /**
         * Retorna o tipo de conta com o mesmo valor passado como parâmetro, no atributo itauErpParameterName.
         */
        fun fromItauErpParameterName(itauErpParameterName: String): TipoConta? {
            return when (itauErpParameterName.toUpperCase()) {
                TipoConta.POUPANCA.itauErpParameterName -> POUPANCA
                TipoConta.CORRENTE.itauErpParameterName -> CORRENTE
                else -> null
            }
        }
    }
}