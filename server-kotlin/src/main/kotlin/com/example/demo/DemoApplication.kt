package com.example.demo

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.http.HttpMethod
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.session.data.mongo.config.annotation.web.reactive.EnableMongoWebSession
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.router
import org.springframework.web.server.session.HeaderWebSessionIdResolver
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime

@SpringBootApplication
@EnableMongoWebSession
class DemoApplication


val beans = beans {
    bean {
        CommandLineRunner {
            println("start data initialization...")
            val posts = ref<PostRepository>()
// see: https://stackoverflow.com/questions/53743766/the-difference-between-concat-and-thenmany-in-reactor
//            Flux.concat(
//                    posts.deleteAll(),
//                    posts.saveAll(
//                            arrayListOf(
//                                    Post(null, "my first post", "content of my first post"),
//                                    Post(null, "my second post", "content of my second post")
//                            )
//                    )
//            )
            posts.deleteAll()
                    .thenMany<Post>(
                            posts.saveAll(
                                    arrayListOf(
                                            Post(null, "my first post", "content of my first post"),
                                            Post(null, "my second post", "content of my second post")
                                    )
                            )
                    )
                    .log()
                    .subscribe(null, null, { println("data initialization done.") })
        }
    }

    bean {
        PostHandler(ref(), ref())
    }

    bean<UserInfoHandler>()

    bean {
        PostRoutes(ref(), ref()).routes()
    }

    bean {
        HeaderWebSessionIdResolver().apply {
            headerName = "X-AUTH-TOKEN"
        }
    }

    profile("cors") {
        bean("corsFilter") {

            //        val config = CorsConfiguration().apply {
//            // allowedOrigins = listOf("http://allowed-origin.com")
//            // maxAge = 8000L
//            // addAllowedMethod("PUT")
//            // addAllowedHeader("X-Allowed")
//        }
//
//        val source = UrlBasedCorsConfigurationSource().apply {
//            registerCorsConfiguration("/**", config)
//        }

            CorsWebFilter { CorsConfiguration().applyPermitDefaultValues() }
        }
    }

    bean {
        PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    bean<SecurityWebFilterChain> {
        //@formatter:off
        ref<ServerHttpSecurity>()
                    .csrf().disable()
                    .httpBasic().securityContextRepository(WebSessionServerSecurityContextRepository())
                .and()
                    .authorizeExchange()
                    .pathMatchers("/auth/**").authenticated()
                    .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                    .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                    .pathMatchers("/posts/**").authenticated()
                    //.pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                    .anyExchange().permitAll()
                .and()
                    .build()
        //@formatter:on

    }

    bean {
        val passwordEncoder = ref<PasswordEncoder>()
        val user = User.withUsername("user")
                .passwordEncoder { passwordEncoder.encode(it) }
                .password("password")
                .roles("USER").build()
        val admin = User.withUsername("admin")
                .password("password")
                .passwordEncoder { passwordEncoder.encode(it) }
                .roles("USER", "ADMIN")
                .build()
        MapReactiveUserDetailsService(user, admin)
    }

}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args) {
        addInitializers(beans)
    }
}

class PostRoutes(private val postHandler: PostHandler, private val userInfoHandler: UserInfoHandler) {
    fun routes() = router {
        "/posts".nest {
            GET("", postHandler::all)
            GET("/count", postHandler::count)
            GET("/{id}", postHandler::get)
            POST("", postHandler::create)
            PUT("{id}", postHandler::update)
            PATCH("{id}", postHandler::updateStatus)
            DELETE("/{id}", postHandler::delete)
            //comments
            GET("/{id}/comments/count", postHandler::countCommentsOfPost)
            GET("/{id}/comments", postHandler::getCommentsOfPost)
            POST("/{id}/comments", postHandler::createComment)

            //get user info

        }
        "/auth".nest {
            GET("/user", userInfoHandler::userInfo)
            GET("/logout", userInfoHandler::logout)
        }
    }
}

class UserInfoHandler {
    fun userInfo(req: ServerRequest): Mono<ServerResponse> {
        return req.principal()
                .map { user ->
                    mapOf<String, Any>(
                            "user" to user.name,
                            "roles" to (user as Authentication).authorities.map { it.authority })
                }
                .flatMap { ok().syncBody(it) }
    }

    fun logout(req: ServerRequest): Mono<ServerResponse> {
        return req.session()
                .doOnNext { it.invalidate() }
                .then()
                .flatMap { ok().build() }
    }
}

class PostHandler(private val posts: PostRepository, private val comments: CommentRepository) {

    fun all(req: ServerRequest): Mono<ServerResponse> {
        return ok().body(this.posts.findAll(), Post::class.java)
    }

    fun count(req: ServerRequest): Mono<ServerResponse> {
        return ok().body(this.posts.count().map { Count(count = it) }, Count::class.java)
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {
        return req.bodyToMono(Post::class.java)
                .flatMap { this.posts.save(it) }
                .flatMap { created(URI.create("/posts/" + it.id)).build() }
    }

    fun get(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
                .flatMap { ok().body(Mono.just(it), Post::class.java) }
                .switchIfEmpty(notFound().build())
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
                .zipWith(req.bodyToMono(Post::class.java))
                .map { it.t1.copy(title = it.t2.title, content = it.t2.content) }
                .flatMap { this.posts.save(it) }
                .flatMap { noContent().build() }
    }

    fun updateStatus(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.findById(req.pathVariable("id"))
                .zipWith(req.bodyToMono(Post::class.java))
                .map { it.t1.copy(status = it.t2.status) }
                .flatMap { this.posts.save(it) }
                .flatMap { noContent().build() }
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        return this.posts.deleteById(req.pathVariable("id"))
                .flatMap { noContent().build() }
    }


    fun createComment(req: ServerRequest): Mono<ServerResponse> {
        val id = req.pathVariable("id")
        val postId = PostId(id)

        return req.bodyToMono(Comment::class.java)
                .map { it.apply { post = postId } }
                .flatMap { this.comments.save(it) }
                .flatMap { created(URI.create("/posts/" + id + "/comments" + it.id)).build() }
    }

    fun countCommentsOfPost(req: ServerRequest): Mono<ServerResponse> {
        val id = req.pathVariable("id")
        val postId = PostId(id)
        return ok().body(this.comments.findByPost(postId).count().map { Count(count = it) }, Count::class.java)
    }

    fun getCommentsOfPost(req: ServerRequest): Mono<ServerResponse> {
        val id = req.pathVariable("id")
        val postId = PostId(id)

        return ok().body(this.comments.findByPost(postId), Comment::class.java)
    }
}


//class PostNotFoundException(private val postId: String) : RuntimeException(String.format("Post: %s is not found", postId))

data class Username(var username: String? = null)

data class PostId(var id: String? = null)

data class CommentForm(var content: String? = null)

data class Count(val count: Long)

@Document
data class Post(
        @Id var id: String? = null,
        var title: String? = null,
        var content: String? = null,
        val status: Status = Status.DRAFT,
        @CreatedDate var createdDate: LocalDateTime = LocalDateTime.now()
)

enum class Status {
    DRAFT, PUBLISHED
}

interface PostRepository : ReactiveMongoRepository<Post, String>

@Document
data class Comment(
        @Id var id: String? = null,
        var content: String? = null,
        var post: PostId? = null,
        @CreatedDate var createdDate: LocalDateTime = LocalDateTime.now()
)

interface CommentRepository : ReactiveMongoRepository<Comment, String> {

    fun findByPost(id: PostId): Flux<Comment>
}

//
//@Document
//data class User(
//        @Id var id: String? = null,
//        var username: String? = null,
//        var password: String? = null,
//        var email: String? = null,
//        var active: Boolean = true,
//        var roles: List<String> = mutableListOf()
//)
//
//interface UserRepository : ReactiveMongoRepository<User, String>
