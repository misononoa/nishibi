package cc.misononoa.nishibi.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import cc.misononoa.nishibi.orm.entity.Post;
import cc.misononoa.nishibi.orm.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostRepository repository;

    public Page<Post> getPosts(@NonNull Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Post> save(@NonNull Post post) {
        try {
            var result = repository.save(post);
            return Optional.of(result);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Post> get(@NonNull UUID id) {
        return repository.findById(id);
    }

    public Optional<Post> get(@NonNull String id) {
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
        return repository.findByAbbrevHash(postHash);
    }

}
