package com.example.demo.interfaces

import com.example.demo.interfaces.dto.CreateCommentCommand
import com.example.demo.interfaces.dto.CreatePostCommand
import com.example.demo.interfaces.dto.UpdatePostCommand
import com.example.demo.domain.exception.PostNotFoundException
import com.example.demo.domain.model.Comment
import com.example.demo.domain.model.Post
import com.example.demo.domain.repository.CommentRepository
import com.example.demo.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.util.*
import javax.validation.Valid

@RestController()
@RequestMapping("/posts")
@Validated
class PostController(
    private val posts: PostRepository,
    private val comments: CommentRepository
) {

    @GetMapping
    fun allPosts(): ResponseEntity<Flow<Post>> {
        return ok(posts.findAll())
    }

    @PostMapping
    suspend fun create(@RequestBody @Valid data: CreatePostCommand): ResponseEntity<Any> {
        val post = Post(title = data.title, content = data.content)
        val saved = posts.save(post)
        return created(URI.create("/posts/${saved.id}")).build()
    }

    @GetMapping("/{id}")
    suspend fun getPost(@PathVariable("id") id: UUID): ResponseEntity<Post> {
        val post = posts.findById(id) ?: throw PostNotFoundException(id)
        return ok(post)
    }

    @PutMapping("/{id}")
    suspend fun update(@PathVariable("id") id: UUID, @RequestBody @Valid data: UpdatePostCommand): ResponseEntity<Any> {
        val post = posts.findById(id) ?: throw PostNotFoundException(id)
        post.apply {
            title = data.title
            content = data.content
        }
        posts.save(post)
        return noContent().build()
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable("id") id: UUID): ResponseEntity<Any> {
        val post = posts.findById(id) ?: throw PostNotFoundException(id)
        posts.delete(post)
        return noContent().build()
    }

    @GetMapping("/{id}/comments")
    suspend fun getComments(@PathVariable("id") id: UUID): ResponseEntity<Flow<Comment>> {
        if (!posts.existsById(id)) throw PostNotFoundException(id)
        return ok(comments.findByPostId(id))
    }

    @PostMapping("/{id}/comments")
    suspend fun createComment(
        @PathVariable("id") id: UUID,
        @RequestBody @Valid data: CreateCommentCommand
    ): ResponseEntity<Any> {
        val post = posts.findById(id) ?: throw PostNotFoundException(id)
        val comment = Comment(content = data.content, postId = post.id!!)
        val savedComment = comments.save(comment)
        return created(URI.create("/comments/${savedComment.id}")).build()
    }
}