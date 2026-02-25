package cc.misononoa.nishibi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import cc.misononoa.nishibi.model.entity.Post;
import cc.misononoa.nishibi.model.entity.PostRelation;

public interface PostRelationRepository extends JpaRepository<PostRelation, UUID> {

    public List<PostRelation> findByRelatedPost(Post relatedPost);

}