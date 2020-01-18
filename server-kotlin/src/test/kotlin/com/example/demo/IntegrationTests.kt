package com.example.demo


import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.reactive.function.client.WebClient
import reactor.kotlin.test.test
import reactor.test.StepVerifier

@SpringBootTest(classes = [DemoApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = arrayOf(TestConfigInitializer::class))
@EnableAutoConfiguration(exclude = arrayOf(EmbeddedMongoAutoConfiguration::class))
class IntegrationTests {

    private lateinit var client: WebClient

    @LocalServerPort
    private var port: Int = 8080

    @BeforeAll
    fun setup() {
        client = WebClient.create("http://localhost:$port")
    }

    @Test
    fun `get all posts`() {
        client.get()
                .uri("/posts")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .test()
                .consumeNextWith {
                    StepVerifier.create(it.bodyToFlux(Post::class.java))
                            .consumeNextWith { it2 -> assert(it2.title == "post one") }
                            .consumeNextWith { it3 -> assert(it3.title == "post two") }
                            //.verifyComplete()
                }
                .verifyComplete()
    }

    @Test
    fun `get none existing post should return 404`() {
        client.get()
                .uri("/posts/notexisted")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.NOT_FOUND }
                .verifyComplete()
    }

    @Test
    fun `create a post without auth should fail with 401`() {
        client.post()
                .uri("/posts/notexisted")
                .bodyValue(Post(content = "test post"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .test()
                .expectNextMatches { it.statusCode() == HttpStatus.UNAUTHORIZED }
                .verifyComplete()
    }

    @Test
    fun `create a post  auth should fail with 401`() {
        client.post()
                .uri("/posts")
                .bodyValue(Post(content = "test post"))
                .headers {
                    it.setBasicAuth("user", "password")
                    it.contentType = MediaType.APPLICATION_JSON
                }
                .exchange()
                .test()
                .expectNextMatches {
                    println(it.statusCode())
                    it.statusCode() == HttpStatus.CREATED
                }
                .verifyComplete()
    }
}
