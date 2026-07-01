package cc.misononoa.nishibi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import cc.misononoa.nishibi.core.web.model.RequestInfo;
import cc.misononoa.nishibi.model.entity.Post;
import cc.misononoa.nishibi.model.entity.PostRelation;
import cc.misononoa.nishibi.model.form.PostForm;
import cc.misononoa.nishibi.repository.PostRelationRepository;
import cc.misononoa.nishibi.repository.PostRepository;

/**
 * サービス層の単体テスト。SpringコンテキストもDBも使わず、Repositoryを純粋なMockitoモックに差し替える。
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostRelationRepository relationRepository;

    @InjectMocks
    private PostService postService;

    private static final RequestInfo REQUEST_INFO = new RequestInfo(
            "203.0.113.7",
            Instant.parse("2026-01-02T03:04:05Z"),
            ZoneId.of("Asia/Tokyo"));

    private static PostForm form(String text) {
        var form = new PostForm();
        form.setText(text);
        return form;
    }

    @Test
    void createPost_savesPostWithSha1HashAndConvertedTimestamp() {
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(relationRepository.findByRelatedPost(any(Post.class))).thenReturn(List.of());

        postService.createPost(form("just a plain post"), REQUEST_INFO);

        var captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        var saved = captor.getValue();

        assertThat(saved.getText()).isEqualTo("just a plain post");
        assertThat(saved.getPostHash()).matches("[0-9a-f]{40}");
        assertThat(saved.getCreatedAt())
                .isEqualTo(LocalDateTime.ofInstant(REQUEST_INFO.requestTime(), REQUEST_INFO.timeZone()));
        // 参照(#hash)を含まないので関連は保存されない
        verify(relationRepository, never()).save(any(PostRelation.class));
    }

    @Test
    void createPost_withReference_savesRelationToReferencedPost() {
        var referenced = Post.builder()
                .id(UUID.randomUUID())
                .postHash("a1b2c3d0000000000000000000000000000000ff")
                .text("referenced post")
                .build();
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(relationRepository.findByRelatedPost(any(Post.class))).thenReturn(List.of());
        when(postRepository.findByAbbrevHash("a1b2c3d")).thenReturn(Optional.of(referenced));

        postService.createPost(form("re: #a1b2c3d thanks"), REQUEST_INFO);

        var captor = ArgumentCaptor.forClass(PostRelation.class);
        verify(relationRepository).save(captor.capture());
        var relation = captor.getValue();
        assertThat(relation.getPost()).isSameAs(referenced);
        assertThat(relation.getRelatedPost().getText()).isEqualTo("re: #a1b2c3d thanks");
    }

    @Test
    void createPost_deduplicatesRepeatedReferences() {
        var referenced = Post.builder()
                .id(UUID.randomUUID())
                .postHash("abcdef1000000000000000000000000000000000")
                .text("target")
                .build();
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(relationRepository.findByRelatedPost(any(Post.class))).thenReturn(List.of());
        when(postRepository.findByAbbrevHash("abcdef1")).thenReturn(Optional.of(referenced));

        // 同じ参照を2回書いても関連は1件だけ
        postService.createPost(form("#abcdef1 and again #abcdef1 "), REQUEST_INFO);

        verify(relationRepository, times(1)).save(any(PostRelation.class));
    }

    @Test
    void createPost_deletesExistingRelationsBeforeReinserting() {
        var existing = PostRelation.builder().id(UUID.randomUUID()).build();
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(relationRepository.findByRelatedPost(any(Post.class))).thenReturn(List.of(existing));

        postService.createPost(form("no references here"), REQUEST_INFO);

        verify(relationRepository).delete(existing);
    }

    @Test
    void getPosts_requestsTenItemsSortedByCreatedAtDescending() {
        Page<Post> emptyPage = new PageImpl<>(List.of());
        when(postRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        var result = postService.getPosts(3);

        assertThat(result).isSameAs(emptyPage);
        var captor = ArgumentCaptor.forClass(Pageable.class);
        verify(postRepository).findAll(captor.capture());
        var pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(3);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        var order = pageable.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "a", "abc", "abcdef" })
    void getByHash_returnsEmptyWithoutHittingRepositoryWhenShorterThanSeven(String hash) {
        assertThat(postService.getByHash(hash)).isEmpty();
        verifyNoInteractions(postRepository);
    }

    @Test
    void getByHash_delegatesToRepositoryWhenLongEnough() {
        var post = Post.builder().id(UUID.randomUUID()).build();
        when(postRepository.findByAbbrevHash("abcdef1")).thenReturn(Optional.of(post));

        assertThat(postService.getByHash("abcdef1")).containsSame(post);
    }

    @Test
    void getByString_delegatesToFindByIdForValidUuid() {
        var id = UUID.randomUUID();
        var post = Post.builder().id(id).build();
        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        assertThat(postService.get(id.toString())).containsSame(post);
    }

    @Test
    void getByString_throwsForInvalidUuid() {
        assertThatThrownBy(() -> postService.get("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(postRepository);
    }
}
