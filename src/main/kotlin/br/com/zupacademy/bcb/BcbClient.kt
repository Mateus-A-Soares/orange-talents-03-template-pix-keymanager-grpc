package br.com.zupacademy.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_XML
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcbService.keys.url}")
interface BcbClient {

    @Post(consumes = [APPLICATION_XML], processes = [APPLICATION_XML])
    fun cadastraChave(@Body request: CadastraChavePixBcbRequest): HttpResponse<CadastraChavePixBcbResponse>
}