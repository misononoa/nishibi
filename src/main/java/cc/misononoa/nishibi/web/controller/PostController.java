package cc.misononoa.nishibi.web.controller;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.FragmentsRendering;

import cc.misononoa.nishibi.model.entity.Post;
import cc.misononoa.nishibi.service.PostService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRetarget;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postsService;

    @HxRequest
    @PostMapping(path = "/post", consumes = MediaType.APPLICATION_JSON_VALUE)
    public FragmentsRendering post(
            @RequestBody @Validated PostDTO dto,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable) {
        postsService.save(dto.toPost())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        var allPosts = postsService.getPosts(pageable);
        return FragmentsRendering
                .fragment("index::post-article", Map.of("posts", allPosts))
                .fragment("index::post-form")
                .fragment("index::pager")
                .build();
    }

    @HxRetarget("#post-form")
    @ExceptionHandler(BindException.class)
    public FragmentsRendering handleValidationError(BindException ex, Model model) {
        model.addAttribute("formError", ex.getFieldError("text").getDefaultMessage());
        return FragmentsRendering.fragment("index::post-form").build();
    }

    @GetMapping("/post")
    public String get(
            Model model,
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable) {
        var posts = postsService.getPosts(pageable);
        model.addAttribute("posts", posts);
        return "index";
    }

    @GetMapping("/post/{abbrevHash}")
    public String get(@PathVariable("abbrevHash") String hash, Model model) {
        var post = postsService.getByHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute(post);
        return "detail";
    }

    public static record PostDTO(
            @NotBlank String postHash,
            @NotBlank(message = "入力してね") String text) {
        private Post toPost() {
            return Post.builder()
                    .postHash(this.postHash())
                    .text(this.text())
                    .build();
        }
    }

}
