package com.example.demo.interfaces;

import com.example.demo.domain.model.Comment;
import com.example.demo.domain.model.Post;
import com.example.demo.domain.model.Status;
import com.example.demo.domain.repository.CommentRepository;
import com.example.demo.domain.repository.PostRepository;
import com.example.demo.interfaces.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static org.springframework.http.HttpStatus.NO_CONTENT;
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
    public Flux<Post> all(@RequestParam(value = "q", required = false) String q,
                          @RequestParam(value = "page", defaultValue = "0") long page,
                          @RequestParam(value = "size", defaultValue = "10") long size) {
        return filterPublishedPostsByKeyword(q)
                .sort(comparing(Post::getCreatedDate).reversed())
                .skip(page * size).take(size);
    }

    @GetMapping(value = "/count")
    public Mono<CountValue> count(@RequestParam(value = "q", required = false) String q) {
        return filterPublishedPostsByKeyword(q).count().log().map(CountValue::new);
    }

    private Flux<Post> filterPublishedPostsByKeyword(String q) {
        return this.posts.findAll()
                .filter(p -> Status.PUBLISHED == p.getStatus())
                .filter(
                        p -> Optional.ofNullable(q)
                                .map(key -> p.getTitle().contains(key) || p.getContent().contains(key))
                                .orElse(true)
                );
    }

    @PostMapping("")
    public Mono<ResponseEntity> create(@RequestBody @Valid CreatPostCommand post) {
        var data = Post.builder()
                .title(post.title())
                .content(post.content())
                .build();
        return this.posts.save(data)
                .map(saved -> created(URI.create("/posts/" + saved.getId())).build());
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return this.posts.findById(id).switchIfEmpty(Mono.error(new PostNotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity> update(@PathVariable("id") String id, @RequestBody @Valid UpdatePostCommand post) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .map(p -> {
                    p.setTitle(post.title());
                    p.setContent(post.content());
                    return p;
                })
                .flatMap(this.posts::save)
                .map(data -> noContent().build());
    }

    @PutMapping("/{id}/status")
    public Mono<ResponseEntity> updateStatus(@PathVariable("id") String id, @RequestBody @Valid StatusUpdateRequest body) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .map(p -> {
                    // TODO: check if the current user is author it has ADMIN role.
                    p.setStatus(Status.valueOf(body.status()));
                    return p;
                })
                .flatMap(this.posts::save)
                .map(data -> noContent().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity> delete(@PathVariable("id") String id) {
        return this.posts.findById(id)
                .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
                .flatMap(this.posts::delete)
                .map(data -> noContent().build());
    }

    @GetMapping("/{id}/comments")
    public Flux<Comment> getCommentsOf(@PathVariable("id") String id) {
        return this.posts.findById(id)
                .flatMapMany(p -> Flux.fromIterable(p.getComments()));
    }

    @PostMapping("/{id}/comments")
    public Mono<ResponseEntity> createCommentsOf(@PathVariable("id") String id, @RequestBody @Valid CommentForm form) {
        return this.posts.addComment(id, form.content())
                .map(saved -> created(URI.create("/posts/" + id + "/comments/" + saved.getId())).build());
    }

}
