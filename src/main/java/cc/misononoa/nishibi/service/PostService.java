package cc.misononoa.nishibi.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cc.misononoa.nishibi.core.web.model.RequestInfo;
import cc.misononoa.nishibi.logic.PostHashLogic;
import cc.misononoa.nishibi.model.entity.Post;
import cc.misononoa.nishibi.model.entity.PostRelation;
import cc.misononoa.nishibi.model.form.PostForm;
import cc.misononoa.nishibi.repository.PostRelationRepository;
import cc.misononoa.nishibi.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostRelationRepository relationRepository;

    @Transactional
    public Post createPost(PostForm form, RequestInfo info) {
        var hash = PostHashLogic.generate(form.getContent(), info.remoteAddr(), info.requestTime());
        var p = Post.builder().content(form.getContent())
                .postHash(hash)
                .createdAt(LocalDateTime.ofInstant(info.requestTime(), info.timeZone()))
                .build();
        var result = postRepository.save(p);
        savePostRelation(result);
        return result;
    }

    private void savePostRelation(Post post) {
        // 一応再登録にする
        relationRepository.findByRelatedPost(post).stream()
                .forEach(relationRepository::delete);
        PostHashLogic.extract(post.getContent()).stream()
                .map(this::getByHash)
                .flatMap(Optional::stream)
                .distinct()
                .map(r -> {
                    return PostRelation.builder().post(r).relatedPost(post).build();
                })
                .forEach(relationRepository::save);
    }

    public Page<Post> getPosts(Integer page) {
        var pageRequest = PageRequest.of(page, 10)
                .withSort(Sort.by(Post::getCreatedAt).descending());
        return postRepository.findAll(pageRequest);
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

}
