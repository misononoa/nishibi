package cc.misononoa.nishibi.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cc.misononoa.nishibi.model.entity.Post;
import cc.misononoa.nishibi.service.PostService;

/**
 * IndexController の単体テスト。Webレイヤは実際に起動するが {@link PostService} をモック化してコントローラを分離する。
 * DBオートコンフィグは test プロファイルで除外しているためDB接続は発生しない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndexControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PostService postService;

    @Test
    void index_rendersIndexViewWithFormAndPosts() throws Exception {
        when(postService.getPosts(0)).thenReturn(new PageImpl<Post>(List.of()));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attributeExists("posts"));
    }

    @Test
    void credits_rendersCreditsView() throws Exception {
        mvc.perform(get("/credits"))
                .andExpect(status().isOk())
                .andExpect(view().name("credits"));
    }
}
