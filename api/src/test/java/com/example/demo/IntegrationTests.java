package com.example.demo;

import com.example.demo.application.SessionConfig;
import com.example.demo.domain.model.Comment;
import com.example.demo.domain.model.Post;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "embedded.mongodb.enabled=true",
                "embedded.mongodb.install.enabled=true",
                "spring.data.mongodb.uri=mongodb://${embedded.mongodb.host}:${embedded.mongodb.port}/${embedded.mongodb.database}"
        }
)
@Slf4j
public class IntegrationTests {

    @LocalServerPort
    int port;

    WebTestClient client;

    String token;

    @BeforeEach
    public void setup() {
        client = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        var exchangeResult = client.post().uri("/login").bodyValue(Map.of("username", "uesr", "password", "password"))
                .exchange().returnResult(Object.class);
        var headers = exchangeResult.getResponseHeaders();
        log.debug("headers: {}", headers);
        token = headers.get(SessionConfig.xAuthToken).get(0);
    }

    @Test
    public void getAllPostsWithAuthentication_ShouldBeOk() {
        client
                .get()
                .uri("/posts/")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    public void getNoneExistedPost_ShouldReturn404() {
        client
                .get()
                .uri("/posts/ABC")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void createPostWithoutAuthentication_shouldReturn401() {
        client
                .post()
                .uri("/posts")
                .body(BodyInserters.fromValue(Post.builder().title("Post test").content("content of post test").build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void updateNoneExistedPostWithUserRole_shouldReturn404() {
        client
                .mutate().defaultHeaders(httpHeaders -> httpHeaders.set(SessionConfig.xAuthToken, token)).build()
                .put()
                .uri("/posts/none_existed")
                .body(BodyInserters.fromValue(Post.builder().title("updated title").content("updated content").build()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deletePostWithUserRole_shouldReturn403() {
        client
                .mutate().defaultHeaders(httpHeaders -> httpHeaders.set(SessionConfig.xAuthToken, token)).build()
                .delete()
                .uri("/posts/1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteNoneExistedPostWithAdminRole_shouldReturn404() {
        client
                .mutate().defaultHeaders(httpHeaders -> httpHeaders.set(SessionConfig.xAuthToken, token)).build()
                .delete()
                .uri("/posts/none_existed")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void postCrudOperations() {
        int randomInt = new Random().nextInt();
        String title = "Post test " + randomInt;
        String content = "content of " + title;


        var result = client
                .mutate().defaultHeaders(httpHeaders -> httpHeaders.set(SessionConfig.xAuthToken, token)).build()
                .post()
                .uri("/posts")
                .bodyValue(Post.builder().title(title).content(content).build())
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Void.class);


        String savedPostUri = result.getResponseHeaders().getLocation().toString();

        assertNotNull(savedPostUri);

        client
                .get()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.content").isEqualTo(content)
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.createdBy.username").isEqualTo("user")
                .jsonPath("$.lastModifiedDate").isNotEmpty()
                .jsonPath("$.lastModifiedBy.username").isEqualTo("user");

        // added comment
        client
                .mutate().defaultHeaders(httpHeaders -> httpHeaders.set(SessionConfig.xAuthToken, token)).build()
                .post()
                .uri(savedPostUri + "/comments")
                .bodyValue(Comment.builder().content("my comments").build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        // get comments of post
        client
                .get()
                .uri(savedPostUri + "/comments")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Comment.class).hasSize(1);

        String updatedTitle = "updated title";
        String updatedContent = "updated content";
        client
                .mutate().filter(basicAuthentication("admin", "password")).build()
                .put()
                .uri(savedPostUri)
                .bodyValue(Post.builder().title(updatedTitle).content(updatedContent).build())
                .exchange()
                .expectStatus().isNoContent();

        //verified updated.
        client
                .get()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(updatedTitle)
                .jsonPath("$.content").isEqualTo(updatedContent)
                .jsonPath("$.createdDate").isNotEmpty()
                .jsonPath("$.createdBy.username").isEqualTo("user")
                .jsonPath("$.lastModifiedDate").isNotEmpty()
                .jsonPath("$.lastModifiedBy.username").isEqualTo("admin");


        client
                .mutate().defaultHeaders(httpHeaders -> httpHeaders.set(SessionConfig.xAuthToken, token)).build()
                .delete()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isForbidden();

        client
                .mutate().defaultHeaders(httpHeaders -> httpHeaders.set(SessionConfig.xAuthToken, token)).build()
                .delete()
                .uri(savedPostUri)
                .exchange()
                .expectStatus().isNoContent();

    }

}
