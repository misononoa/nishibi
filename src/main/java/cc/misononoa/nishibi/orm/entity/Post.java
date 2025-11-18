package cc.misononoa.nishibi.orm.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Data
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(schema = "public", name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @CreatedDate
    @Column(nullable = false)
    public LocalDateTime createdAt;

    @NotBlank
    @Column(name = "post_hash", nullable = false, unique = true, length = 40)
    public String postHash;

    @Formula("substring(post_hash for 7)")
    public String abbrevHash;

    public String getAbbrevHash() {
        if (StringUtils.isBlank(abbrevHash)) {
            abbrevHash = StringUtils.substring(postHash, 0, 7);
        }
        return abbrevHash;
    }

    @NotEmpty
    @Column(nullable = false, columnDefinition = "text")
    public String text;

}
