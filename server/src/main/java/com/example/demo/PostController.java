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
        return filterByKeyword(q)
            .sort(comparing(Post::getCreatedDate).reversed())
            .skip(page * size).take(size);
    }

    @GetMapping(value = "/count")
    public Mono<Count> count(@RequestParam(value = "q", required = false) String q) {
        return filterByKeyword(q).count().log().map(Count::new);
    }

    private Flux<Post> filterByKeyword(String q) {
        return this.posts.findAll()
            .filter(p -> Optional.ofNullable(q).map(key -> p.getTitle().contains(key) || p.getContent().contains(key)).orElse(true));
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

    @PostMapping("/{id}/comments")
    public Mono<Comment> createCommentsOf(@PathVariable("id") String id, @RequestBody @Valid CommentForm form) {
        Comment comment = Comment.builder()
            .post(new PostId(id))
            .content(form.getContent())
            .build();

        return this.comments.save(comment);
    }

}
