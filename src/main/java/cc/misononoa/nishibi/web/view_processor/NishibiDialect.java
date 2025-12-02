package cc.misononoa.nishibi.web.view_processor;

import java.util.Set;
import java.util.regex.Pattern;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

@Component
public class NishibiDialect extends AbstractProcessorDialect {

    public static final String DIALECT_PREFIX = "nishibi";

    private final Parser parser;
    private final HtmlRenderer renderer;

    protected NishibiDialect(Parser parser, HtmlRenderer renderer) {
        super("PostLinkDialect", DIALECT_PREFIX, 1000);
        this.parser = parser;
        this.renderer = renderer;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        return Set.of(new NishibiMdProcessor(dialectPrefix));
    }

    class NishibiMdProcessor extends AbstractAttributeTagProcessor {

        // #{abbrevHash}形式の投稿リンクにマッチする正規表現
        private static final Pattern POST_LINK_PATTERN = Pattern.compile(
                "#(\\w{7,40})(?=\\s|<|$|[、。！？])");

        private static final String ATTR_NAME = "render";
        private static final int PRECEDENCE = 10000;

        public NishibiMdProcessor(final String dialectPrefix) {
            super(TemplateMode.HTML,
                    dialectPrefix,
                    null,
                    false,
                    ATTR_NAME,
                    true,
                    PRECEDENCE,
                    true);
        }

        @Override
        protected void doProcess(
                ITemplateContext context,
                IProcessableElementTag tag,
                AttributeName attributeName,
                String attributeValue,
                IElementTagStructureHandler structureHandler) {
            var expression = StandardExpressions
                    .getExpressionParser(context.getConfiguration())
                    .parseExpression(context, attributeValue);
            if (!(expression.execute(context) instanceof String textContent)) {
                throw new ProcessingException("テキストの処理に失敗しました");
            }

            var markdownHtml = renderer.render(parser.parse(textContent));

            var withLinks = processPostLinks(markdownHtml);

            structureHandler.setBody(withLinks, false);
        }

        private String processPostLinks(String html) {
            var matcher = POST_LINK_PATTERN.matcher(html);
            var result = new StringBuilder();

            while (matcher.find()) {
                if (!(matcher.group(1) instanceof String abbrevHash)) {
                    continue;
                }

                var link = String.format(
                        "<a href=\"/posts/%s\" class=\"post-quote-link\">#%s</a>",
                        HtmlUtils.htmlEscape(abbrevHash),
                        HtmlUtils.htmlEscape(abbrevHash));

                matcher.appendReplacement(result, escapeDollarSigns(link));
            }
            matcher.appendTail(result);

            return result.toString();
        }

        private String escapeDollarSigns(String text) {
            return text.replace("$", "\\$");
        }

    }

}
