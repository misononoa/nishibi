package cc.misononoa.nishibi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import cc.misononoa.nishibi.model.form.PostForm;
import cc.misononoa.nishibi.service.PostService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/")
@Controller
public class IndexController {

    private final PostService postsService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("posts", postsService.getPosts(0));
        return "index";
    }

    @GetMapping("credits")
    public String credits() {
        return "credits";
    }

}
