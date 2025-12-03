package cc.misononoa.nishibi.orm.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cc.misononoa.nishibi.model.entity.Post;

public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query("""
                select p
                from Post p
                where
                    p.postHash like :hash || '%'
                order by
                    p.createdAt desc
                limit 1
            """)
    Optional<Post> findByAbbrevHash(@Param("hash") String abbrevHash);

}
