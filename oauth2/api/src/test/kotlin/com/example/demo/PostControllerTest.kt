package com.example.demo

import com.example.demo.domain.model.Post
import com.example.demo.domain.repository.CommentRepository
import com.example.demo.domain.repository.PostRepository
import com.example.demo.interfaces.PostController
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@WebFluxTest(value = [PostController::class])
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PostControllerTest {

    @MockkBean
    private lateinit var posts: PostRepository

    @MockkBean
    private lateinit var comments: CommentRepository

    @Autowired
    private lateinit var client: WebTestClient

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `get all posts`() {
        coEvery { posts.findAll() } returns flowOf(
            Post(
                id = UUID.randomUUID(),
                title = "test title",
                content = "test content"
            )
        )

        client.get()
            .uri("/posts").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Post::class.java).hasSize(1)

        coVerify(exactly = 1) { posts.findAll() }
    }

    @Test
    fun `get single post by id`() {
        coEvery { posts.findById(any<UUID>()) } returns
                Post(
                    id = UUID.randomUUID(),
                    title = "test title",
                    content = "test content"
                )

        val id = UUID.randomUUID()
        client.get()
            .uri("/posts/$id").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.title").isEqualTo("test title")

        coVerify(exactly = 1) { posts.findById(id) }
    }

    @Test
    fun `get single post by non-existing id`() {
        coEvery { posts.findById(any<UUID>()) } returns null

        val id = UUID.randomUUID()
        client.get()
            .uri("/posts/$id").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.findById(id) }
    }

    @Test
    fun `create a post`() {
        val id = UUID.randomUUID()
        coEvery { posts.save(any<Post>()) } returns
                Post(
                    id = id,
                    title = "update title",
                    content = "update content"
                )

        client.post()
            .uri("/posts").contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Post(title = "update title", content = "update content"))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().location("/posts/$id")


        coVerify(exactly = 1) { posts.save(any<Post>()) }
    }

    @Test
    fun `update a post`() {
        val id = UUID.randomUUID()
        coEvery { posts.findById(any<UUID>()) } returns
                Post(
                    id = id,
                    title = "test title",
                    content = "test content"
                )
        coEvery { posts.save(any<Post>()) } returns
                Post(
                    id = id,
                    title = "test title",
                    content = "test content"
                )

        client.put()
            .uri("/posts/$id").contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Post(title = "test title", content = "test content"))
            .exchange()
            .expectStatus().isNoContent
        coVerify(exactly = 1) { posts.findById(id) }
        coVerify(exactly = 1) { posts.save(any<Post>()) }
    }

    @Test
    fun `delete a post`() {
        val id = UUID.randomUUID()
        coEvery { posts.deleteById(any<UUID>()) } returns Unit

        client.delete()
            .uri("/posts/$id")
            .exchange()
            .expectStatus().isNoContent

        coVerify(exactly = 1) { posts.deleteById(id) }
    }

}