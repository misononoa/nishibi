package cc.misononoa.nishibi.model.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UuidGenerator.Style;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Data
@Entity
@Table(schema = "public", name = "post_relation", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "post_id", "related_post_id" }) })
public class PostRelation {

    @Id
    @UuidGenerator(style = Style.VERSION_7)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    public UUID id;

    @ManyToOne
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    public Post post;

    @ManyToOne
    @JoinColumn(name = "related_post_id", referencedColumnName = "id")
    public Post relatedPost;

}
