package com.example.demo;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController()
@RequestMapping(value = "/posts")
class PostController {

    private final PostRepository posts;

    private final CommentRepository comments;

    public PostController(PostRepository posts, CommentRepository comments) {
        this.posts = posts;
        this.comments = comments;
    }

    @GetMapping("")
    public Flux<Post> all(@RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "page", defaultValue = "0") long page,
        @RequestParam(value = "size", defaultValue = "10") long size) {
        return filterPublishedPostsByKeyword(q)
            .sort(comparing(Post::getCreatedDate).reversed())
            .skip(page * size).take(size);
    }

    @GetMapping(value = "/count")
    public Mono<Count> count(@RequestParam(value = "q", required = false) String q) {
        return filterPublishedPostsByKeyword(q).count().log().map(Count::new);
    }

    private Flux<Post> filterPublishedPostsByKeyword(String q) {
        return this.posts.findAll()
            .filter(p -> Post.Status.PUBLISHED == p.getStatus())
            .filter(
                p -> Optional.ofNullable(q)
                    .map(key -> p.getTitle().contains(key) || p.getContent().contains(key))
                    .orElse(true)
            );
    }

    @PostMapping("")
    public Mono<Post> create(@RequestBody @Valid Post post) {
        return this.posts.save(post);
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return this.posts.findById(id).switchIfEmpty(Mono.error(new PostNotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") String id, @RequestBody @Valid Post post) {
        return this.posts.findById(id)
            .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
            .map(p -> {
                p.setTitle(post.getTitle());
                p.setContent(post.getContent());

                return p;
            })
            .flatMap(this.posts::save);
    }

    @PutMapping("/{id}/status")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> updateStatus(@PathVariable("id") String id, @RequestBody @Valid StatusUpdateRequest status) {
        return this.posts.findById(id)
            .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
            .map(p -> {
                // TODO: check if the current user is author or it has ADMIN role.
                p.setStatus(Post.Status.valueOf(status.getStatus()));

                return p;
            })
            .flatMap(this.posts::save)
            .flatMap((p) -> Mono.empty());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@PathVariable("id") String id) {
        return this.posts.findById(id)
            .switchIfEmpty(Mono.error(new PostNotFoundException(id)))
            .flatMap(this.posts::delete);
    }

    @GetMapping("/{id}/comments")
    public Flux<Comment> getCommentsOf(@PathVariable("id") String id) {
        return this.comments.findByPost(new PostId(id));
    }

    @GetMapping("/{id}/comments/count")
    public Mono<Count> getCommentsCountOf(@PathVariable("id") String id) {
        return this.comments.findByPost(new PostId(id)).count().log().map(Count::new);
    }

    @PostMapping("/{id}/comments")
    public Mono<Comment> createCommentsOf(@PathVariable("id") String id, @RequestBody @Valid CommentForm form) {
        Comment comment = Comment.builder()
            .post(new PostId(id))
            .content(form.getContent())
            .build();

        return this.comments.save(comment);
    }

}
