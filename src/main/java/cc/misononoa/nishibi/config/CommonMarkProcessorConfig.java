package cc.misononoa.nishibi.config;

import java.util.List;

import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonMarkProcessorConfig {

    @Bean
    Parser createParser() {
        var extensions = List.of(AutolinkExtension.create());
        return Parser.builder()
                .extensions(extensions)
                .build();
    }

    @Bean
    HtmlRenderer createRenderer() {
        return HtmlRenderer.builder()
                .escapeHtml(true)
                .build();
    }

}
