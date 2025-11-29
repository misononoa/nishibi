package cc.misononoa.nishibi.web.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.FragmentsRendering;

import cc.misononoa.nishibi.orm.entity.Post;
import cc.misononoa.nishibi.service.PostsService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PostsController {

    private final PostsService postsService;

    @HxRequest
    @PostMapping(path = "/posts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public FragmentsRendering post(
            @RequestBody PostDTO dto,
            Model model,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable) {
        var post = Post.builder()
                .text(dto.getText())
                .postHash(dto.postHash)
                .build();
        Objects.requireNonNull(post);
        postsService.save(post);

        var allPosts = postsService.getPosts(pageable);
        model.addAttribute("posts", allPosts);
        return FragmentsRendering
                .fragment("index::post-article")
                .fragment("index::newpost-form")
                .fragment("index::pager")
                .build();
    }

    @GetMapping("/posts")
    public String get(Model model,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable) {
        var posts = postsService.getPosts(pageable);
        model.addAttribute("posts", posts);
        return "index";
    }

    @GetMapping("/posts/{abbrevHash}")
    public String get(@PathVariable("abbrevHash") String hash, Model model) {
        var post = postsService.getByHash(hash);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            return "detail";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません");
    }

    @Data
    public static class PostDTO {
        @NotBlank
        private String text;
        @NotBlank
        private String postHash;
    }

}
