package br.com.zupacademy.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_XML
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcbService.keys.url}", errorType = Problem::class)
interface BcbClient {

    @Post(consumes = [APPLICATION_XML], processes = [APPLICATION_XML])
    fun cadastraChave(@Body request: CadastraChavePixBcbRequest): HttpResponse<ChavePixBcbDetailsResponse>

    @Get(value="/{chave}", consumes = [APPLICATION_XML], processes = [APPLICATION_XML])
    fun buscaChave(@PathVariable chave: String): HttpResponse<ChavePixBcbDetailsResponse>

    @Delete(value="/{chave}", consumes = [APPLICATION_XML], processes = [APPLICATION_XML])
    fun deletaChave(@PathVariable chave: String, @Body request: DeletaChavePixBcbRequest): HttpResponse<DeletaChavepPixBcbResponse>
}