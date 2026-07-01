package cc.misononoa.nishibi.e2e;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import cc.misononoa.nishibi.model.entity.Post;

/**
 * 掲示板の主要導線をブラウザ操作で検証するe2eテスト。RepositoryはモックなのでDBには一切接続しない。
 */
class PostBoardE2ETest extends AbstractSelenideE2ETest {

    private static String hashOf(String abbrev) {
        return abbrev + "0".repeat(40 - abbrev.length());
    }

    private static Post samplePost(String abbrev, String text) {
        return Post.builder()
                .id(UUID.randomUUID())
                .postHash(hashOf(abbrev))
                .text(text)
                .createdAt(LocalDateTime.now())
                .postRelations(List.of())
                .build();
    }

    @Test
    void topPage_showsFormAndExistingPosts() {
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(samplePost("a1b2c3d", "hello from selenide"))));

        open("/");

        $("#postform").shouldBe(visible);
        $("#post-list").shouldHave(text("hello from selenide"));
    }

    @Test
    void submittingPost_showsItInTheList() {
        var created = samplePost("bbbccc1", "my brand new post");
        when(postRepository.findByAbbrevHash(anyString())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenReturn(created);
        when(postRelationRepository.findByRelatedPost(any(Post.class))).thenReturn(List.of());
        // 初回のトップ表示は空、投稿後は作成した投稿を返す
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()))
                .thenReturn(new PageImpl<>(List.of(created)));

        open("/");
        $("#postform-text").setValue("my brand new post");
        $("#postform button[type=submit]").click();

        $("#post-list").shouldHave(text("my brand new post"));
    }

    @Test
    void clickingPostLink_opensDetailPage() {
        var post = samplePost("a1b2c3d", "detailed content here");
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(postRepository.findByAbbrevHash("a1b2c3d")).thenReturn(Optional.of(post));

        open("/");
        $("#post-list a").click();

        $("#post-detail").shouldHave(text("detailed content here"));
    }

    @Test
    void visitingUnknownPost_showsNotFoundPage() {
        when(postRepository.findByAbbrevHash(anyString())).thenReturn(Optional.empty());

        open("/post/deadbee");

        $("body").shouldHave(text("404"));
    }

    @Test
    void submittingTooShortPost_showsValidationError() {
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        open("/");
        $("#postform-text").setValue("ab");
        $("#postform button[type=submit]").click();

        $("#postform-wrap label.error").shouldBe(visible);
    }
}
