package br.com.zupacademy.bcb

data class Problem(
    val type: String?,
    val status: Int?,
    val title: String?,
    val detail: String?,
    val violations: Set<Violation>?
)

data class Violation(
    val field: String?,
    val message: String?
)
