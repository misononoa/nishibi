package cc.misononoa.nishibi.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cc.misononoa.nishibi.logic.PostHashLogic;
import cc.misononoa.nishibi.model.entity.Post;
import cc.misononoa.nishibi.model.entity.PostRelation;
import cc.misononoa.nishibi.repository.PostRelationRepository;
import cc.misononoa.nishibi.repository.PostRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostRelationRepository relationRepository;

    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional
    public Optional<Post> createPost(CreatePostDTO dto, String remoteAddr, Instant requestTime) {
        try {
            var p = Post.builder()
                    .text(dto.text)
                    .postHash(PostHashLogic.generate(dto.text(), remoteAddr, requestTime))
                    .createdAt(LocalDateTime.ofInstant(requestTime, ZoneId.systemDefault()))
                    .build();
            var result = postRepository.save(p);
            savePostRelation(result);
            return Optional.of(result);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void savePostRelation(Post post) {
        // 一応再登録にする
        relationRepository.findByRelatedPost(post).stream()
                .forEach(relationRepository::delete);
        PostHashLogic.extract(post.getText()).stream()
                .map(this::getByHash)
                .flatMap(Optional::stream)
                .map(r -> {
                    return PostRelation.builder()
                            .post(r)
                            .relatedPost(post)
                            .build();
                })
                .forEach(relationRepository::save);
    }

    public Optional<Post> get(UUID id) {
        return postRepository.findById(id);
    }

    public Optional<Post> get(String id) {
        if (!(UUID.fromString(id) instanceof UUID uuid)) {
            var message = "\"%s\" is not valid uuid.".formatted(Objects.toString(id, "null"));
            throw new IllegalArgumentException(message);
        }
        return this.get(uuid);
    }

    public Optional<Post> getByHash(String postHash) {
        if (StringUtils.length(postHash) < 7) {
            return Optional.empty();
        }
        return postRepository.findByAbbrevHash(postHash);
    }

    public static record CreatePostDTO(
            @NotBlank(message = "入力してね") String text) {
    }

}
