package com.example.demo

import com.example.demo.application.SecurityConfig
import com.example.demo.application.ValidationConfig
import com.example.demo.domain.model.Post
import com.example.demo.domain.repository.CommentRepository
import com.example.demo.domain.repository.PostRepository
import com.example.demo.interfaces.PostController
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@WebFluxTest(value = [PostController::class])
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class PostControllerTest {

    @TestConfiguration
    @Import(ValidationConfig::class, SecurityConfig::class)
    class TestConfig

    @MockkBean
    private lateinit var posts: PostRepository

    @MockkBean
    private lateinit var comments: CommentRepository

    @Autowired
    private lateinit var client: WebTestClient

    @Value("\${auth0.audience}")
    private lateinit var audience: String

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `get all posts`() = runTest {
        coEvery { posts.findAll() } returns flowOf(
            Post(
                id = UUID.randomUUID(),
                title = "test title",
                content = "test content"
            )
        )

        client
            .get()
            .uri("/posts").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBodyList(Post::class.java).hasSize(1)

        coVerify(exactly = 1) { posts.findAll() }
    }

    @Test
    fun `get single post by id`() = runTest {
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
    fun `get single post by non-existing id`() = runTest {
        coEvery { posts.findById(any<UUID>()) } returns null

        val id = UUID.randomUUID()
        client.get()
            .uri("/posts/$id").accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { posts.findById(id) }
    }

    @Test
    fun `create a post`() = runTest {
        val id = UUID.randomUUID()
        coEvery { posts.save(any<Post>()) } returns
                Post(
                    id = id,
                    title = "test title",
                    content = "test content"
                )

        client
            //.mutateWith(csrf())
            .mutateWith(mockJwt().authorities(SimpleGrantedAuthority("SCOPE_write:posts")))
            .post()
            .uri("/posts").contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Post(title = "test title", content = "test content"))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().location("/posts/$id")

        coVerify(exactly = 1) { posts.save(any<Post>()) }
    }

    @Test
    fun `update a post`() = runTest {
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

        // alternatively use `mockAuthentication` instead.
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user")
            .audience(listOf(audience))
            .build()
        val authorities: Collection<GrantedAuthority> = AuthorityUtils.createAuthorityList("SCOPE_write:posts")
        val token = JwtAuthenticationToken(jwt, authorities)

        client
            //.mutateWith(csrf())
            .mutateWith(mockAuthentication<JwtMutator>(token))
            .put()
            .uri("/posts/$id").contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Post(title = "test title", content = "test content"))
            .exchange()
            .expectStatus().isNoContent
        coVerify(exactly = 1) { posts.findById(id) }
        coVerify(exactly = 1) { posts.save(any<Post>()) }
    }

    @Test
    fun `delete a post`() = runTest {
        val id = UUID.randomUUID()
        coEvery { posts.findById(any<UUID>()) } returns
                Post(
                    id = id,
                    title = "test title",
                    content = "test content"
                )
        coEvery { posts.delete(any<Post>()) } returns Unit

        client
            //.mutateWith(csrf())
            .mutateWith(mockJwt()
                .jwt { it.audience(listOf(audience)).build() }
                .authorities(SimpleGrantedAuthority("SCOPE_delete:posts"))
            )
            .delete()
            .uri("/posts/$id")
            .exchange()
            .expectStatus().isNoContent

        coVerify(exactly = 1) { posts.findById(id) }
        coVerify(exactly = 1) { posts.delete(any<Post>()) }
    }

}