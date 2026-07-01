package cc.misononoa.nishibi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cc.misononoa.nishibi.model.entity.Post;
import cc.misononoa.nishibi.service.PostService;

/**
 * PostController の単体テスト。Webレイヤは起動するが {@link PostService} をモック化してコントローラを分離する。
 * test プロファイルでDBオートコンフィグを除外しているためDB接続は発生しない。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PostService postService;

    @ParameterizedTest
    @ValueSource(strings = { "abc", "hello there", "これは有効な投稿です" })
    void post_withValidForm_invokesCreatePostAndReturnsOk(String text) throws Exception {
        when(postService.getPosts(0)).thenReturn(new PageImpl<Post>(List.of()));

        mvc.perform(post("/post")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("text", text)
                .header("HX-Request", "true")
                .header("HX-Target", "post-list")
                .header("HX-Trigger", "postform"))
                .andExpect(status().isOk());

        verify(postService).createPost(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "  ", "ab" })
    void post_withInvalidForm_returnsValidationFragmentAndSkipsCreate(String text) throws Exception {
        mvc.perform(post("/post")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("text", text)
                .header("HX-Request", "true")
                .header("HX-Target", "post-list")
                .header("HX-Trigger", "postform"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("postform-wrap")));

        verify(postService, never()).createPost(any(), any());
    }

    @Test
    void getPostList_returnsFragmentsForHtmxRequest() throws Exception {
        when(postService.getPosts(1)).thenReturn(new PageImpl<Post>(List.of()));

        mvc.perform(get("/post")
                .param("page", "1")
                .header("HX-Request", "true")
                .header("HX-Target", "post-list"))
                .andExpect(status().isOk());
    }

    @Test
    void getDetail_rendersDetailViewForKnownHash() throws Exception {
        var post = Post.builder()
                .id(UUID.randomUUID())
                .postHash("a1b2c3d0000000000000000000000000000000ff")
                .text("detail body **bold**")
                .createdAt(LocalDateTime.now())
                .postRelations(List.of())
                .build();
        when(postService.getByHash("a1b2c3d")).thenReturn(Optional.of(post));

        mvc.perform(get("/post/a1b2c3d"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(model().attributeExists("post"));
    }

    @Test
    void getDetail_rendersErrorPageForUnknownHash() throws Exception {
        when(postService.getByHash("deadbee")).thenReturn(Optional.empty());

        mvc.perform(get("/post/deadbee"))
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorTitle", "404 not_found"));
    }
}
