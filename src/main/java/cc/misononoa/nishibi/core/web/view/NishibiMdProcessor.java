package cc.misononoa.nishibi.core.web.view;

import java.util.List;

import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import cc.misononoa.nishibi.core.web.exception.ViewProcessingException;
import cc.misononoa.nishibi.logic.PostHashLogic;

public class NishibiMdProcessor extends AbstractAttributeTagProcessor {

    private final Parser parser;
    private final HtmlRenderer renderer;

    private static final String ATTR_NAME = "render";
    private static final int PRECEDENCE = 10000;

    public NishibiMdProcessor(final String dialectPrefix) {
        var extensions = List.of(AutolinkExtension.create());
        this.parser = Parser.builder()
                .extensions(extensions)
                .build();
        this.renderer = HtmlRenderer.builder()
                .escapeHtml(true)
                .build();
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
            throw new ViewProcessingException("テキストの処理に失敗しました");
        }

        var markdownHtml = renderer.render(parser.parse(textContent));

        var withLinks = processPostLinks(markdownHtml);

        structureHandler.setBody(withLinks, false);
    }

    private String processPostLinks(String html) {
        var matcher = PostHashLogic.getMatcher(html);
        var result = new StringBuilder();

        while (matcher.find()) {
            if (!(matcher.group(1) instanceof String abbrevHash)) {
                continue;
            }

            var link = String.format(
                    "<a href=\"/post/%s\" class=\"post-quote-link\">#%s</a>",
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
