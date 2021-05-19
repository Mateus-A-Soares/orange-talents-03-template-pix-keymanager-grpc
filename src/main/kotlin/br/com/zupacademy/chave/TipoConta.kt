package br.com.zupacademy.chave

enum class TipoConta(val itauErpParameterName : String) {
    POUPANCA(itauErpParameterName = "CONTA_POUPANCA"), CORRENTE(itauErpParameterName = "CONTA_CORRENTE")
}
