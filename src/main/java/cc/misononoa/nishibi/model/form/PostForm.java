package cc.misononoa.nishibi.model.form;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostForm {

    @NotBlank
    @Length(min = 3, max = 3000)
    private String text;

}
