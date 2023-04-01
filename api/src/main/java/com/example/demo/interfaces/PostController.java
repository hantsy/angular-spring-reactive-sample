package com.example.demo.interfaces;

import com.example.demo.domain.exception.PostNotFoundException;
import com.example.demo.domain.model.Comment;
import com.example.demo.domain.model.Post;
import com.example.demo.domain.model.Status;
import com.example.demo.domain.repository.CommentRepository;
import com.example.demo.domain.repository.PostRepository;
import com.example.demo.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@RestController()
@RequestMapping(value = "/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostRepository posts;

    private final CommentRepository comments;

    @GetMapping("")
    public Mono<PaginatedResult<PostSummary>> all(@RequestParam(value = "q", required = false) String q,
                                                  @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                  @RequestParam(value = "limit", defaultValue = "10") int limit) {

        return this.posts.findByKeyword(q, offset, limit).collectList()
            .zipWith(this.posts.countByKeyword(q), PaginatedResult::new);
    }

    @PostMapping("")
    public Mono<ResponseEntity> create(@RequestBody @Valid CreatPostCommand post) {
        return this.posts.create(post.title(), post.content())
            .map(saved -> created(URI.create("/posts/" + saved.getId())).build());
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return this.posts.findById(id).switchIfEmpty(Mono.error(new PostNotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity> update(@PathVariable("id") String id, @RequestBody @Valid UpdatePostCommand post) {
        return this.posts.update(id, post.title(), post.content())
            .handle((result, sink) -> {
                if (true) {
                    sink.next(noContent().build());
                } else {
                    sink.error(new PostNotFoundException(id));
                }
            });
    }

    @PutMapping("/{id}/status")
    public Mono<ResponseEntity> updateStatus(@PathVariable("id") String id, @RequestBody @Valid StatusUpdateRequest body) {
        return this.posts.updateStatus(id, Status.valueOf(body.status()))
            .handle((result, sink) -> {
                if (true) {
                    sink.next(noContent().build());
                } else {
                    sink.error(new PostNotFoundException(id));
                }
            });
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity> delete(@PathVariable("id") String id) {
        return this.posts.deleteById(id)
            .handle((result, sink) -> {
                if (true) {
                    sink.next(noContent().build());
                } else {
                    sink.error(new PostNotFoundException(id));
                }
            });
    }

    @GetMapping("/{id}/comments")
    public Flux<Comment> getCommentsOf(@PathVariable("id") String id) {
        return this.posts.findById(id)
            .flatMapMany(p -> Flux.fromIterable(p.getComments()));
    }

    @PostMapping("/{id}/comments")
    public Mono<ResponseEntity> createCommentsOf(@PathVariable("id") String id, @RequestBody @Valid CommentForm form) {
        return this.posts.addComment(id, form.content())
            .map(saved -> noContent().build());
    }

}
