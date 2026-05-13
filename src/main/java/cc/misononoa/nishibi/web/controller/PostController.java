package cc.misononoa.nishibi.web.controller;

import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.FragmentsRendering;

import cc.misononoa.nishibi.core.web.model.RequestInfo;
import cc.misononoa.nishibi.model.form.PostForm;
import cc.misononoa.nishibi.service.PostService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRetarget;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postsService;

    @HxRequest(target = "post-list", triggerId = "postform")
    @PostMapping(path = "/post", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object post(
            @Validated PostForm form,
            @RequestAttribute("requestInfo") RequestInfo info,
            @RequestParam(name = "page", defaultValue = "0") Integer page) {
        postsService.createPost(form, info);
        var posts = postsService.getPosts(page);
        form.setText("");
        return FragmentsRendering
                .fragment("index::post-item", Map.of("posts", posts))
                .fragment("index::postform-wrap")
                .fragment("index::pager")
                .build();
    }

    @HxRetarget("#postform-wrap")
    @ExceptionHandler(BindException.class)
    public FragmentsRendering handleValidationError(BindException ex) {
        return FragmentsRendering.fragment("index::postform-wrap", ex.getModel()).build();
    }

    @HxRequest(target = "post-list")
    @GetMapping("/post")
    public FragmentsRendering get(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") Integer page) {
        var posts = postsService.getPosts(page);
        model.addAttribute("posts", posts);
        return FragmentsRendering
                .fragment("index::post-item")
                .fragment("index::pager")
                .build();
    }

    @GetMapping("/post/{abbrevHash}")
    public String get(@PathVariable("abbrevHash") String hash, Model model) {
        var post = postsService.getByHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute(post);
        return "detail";
    }

}
