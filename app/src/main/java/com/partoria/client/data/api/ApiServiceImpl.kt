package com.partoria.client.data.api

import com.partoria.client.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class ApiServiceImpl(
    private val client: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:8080"
) : ApiService {

    override suspend fun login(request: LoginRequest): LoginResponse {
        println("API CALL: login to $baseUrl/auth/login")
        return client.post {
            url("$baseUrl/auth/login")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun register(request: RegisterRequest): RegisterResponse {
        println("API CALL: register to $baseUrl/auth/register")
        return client.post {
            url("$baseUrl/auth/register")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getAllParts(token: String): List<PartResponse> {
        return client.get {
            url("$baseUrl/parts")
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    override suspend fun getPartWithDetails(token: String, id: Int): PartResponse {
        return client.get {
            url("$baseUrl/parts/$id/details")
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    override suspend fun getFilteredParts(token: String, filter: FilterRequest): PartsResponse {
        println("API CALL: getFilteredParts to $baseUrl/parts/filter")
        return client.post {
            url("$baseUrl/parts/filter")
            contentType(io.ktor.http.ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            setBody(filter)
        }.body()
    }

    override suspend fun getFiltersMeta(token: String): FiltersMetaResponse {
        return client.get {
            url("$baseUrl/parts/filters/meta")
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    override suspend fun getFavorites(token: String): List<PartResponse> {
        return client.get {
            url("$baseUrl/favorites")
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    override suspend fun addToFavorites(token: String, partId: Int) {
        client.post {
            url("$baseUrl/favorites/$partId")
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun removeFromFavorites(token: String, partId: Int) {
        client.delete {
            url("$baseUrl/favorites/$partId")
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun deletePart(token: String, partId: Int) {
        client.delete {
            url("$baseUrl/admin/parts/$partId")
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

    override suspend fun createPart(token: String, part: CreatePartRequest): Int {
        val response = client.post {
            url("$baseUrl/admin/parts")
            contentType(io.ktor.http.ContentType.Application.Json)
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            setBody(part)
        }.body<Map<String, String>>()

        val message = response["message"] ?: throw Exception("No message in response")
        val id = message.substringAfterLast(" ").toIntOrNull() ?: throw Exception("Failed to parse part id")
        return id
    }

    override suspend fun updatePart(token: String, part: UpdatePartRequest) {
        client.put {
            url("$baseUrl/admin/parts")
            contentType(io.ktor.http.ContentType.Application.Json)
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            setBody(part)
        }
    }
}