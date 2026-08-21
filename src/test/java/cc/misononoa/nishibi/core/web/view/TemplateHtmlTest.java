package cc.misononoa.nishibi.core.web.view;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.attoparser.simple.SimpleMarkupParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@TestInstance(Lifecycle.PER_CLASS)
class TemplateHtmlTest {

    private SimpleMarkupParser parser;
    private ResourcePatternResolver resolver;

    @BeforeAll
    void init() {
        var parserConfig = ParseConfiguration.htmlConfiguration();
        parserConfig.setElementBalancing(ParseConfiguration.ElementBalancing.REQUIRE_BALANCED);
        this.parser = new SimpleMarkupParser(parserConfig);
        this.resolver = new PathMatchingResourcePatternResolver();
    }

    Stream<Resource> templateResources() throws IOException {
        return Arrays.stream(resolver.getResources("classpath:/templates/**/*.html"));
    }

    @ParameterizedTest
    @MethodSource("templateResources")
    void isWellFormedMarkup(Resource template) throws IOException {
        try (var reader = new BufferedReader(new FileReader(template.getFile()))) {
            var handler = new AbstractSimpleMarkupHandler() {
            };
            parser.parse(reader, handler);
        } catch (ParseException e) {
            var path = template.getFilePath().toString();
            fail("テンプレート " + path + " のマークアップが不正です: " + e.getMessage(), e);
        }
    }

}
