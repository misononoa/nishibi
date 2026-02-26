package cc.misononoa.nishibi.web.controller;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import cc.misononoa.nishibi.logic.PostHashLogic;
import cc.misononoa.nishibi.web.controller.PostController.PostDTO;
import jakarta.servlet.http.HttpServletRequest;

@Component
@ControllerAdvice
public class PostHashGenAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(
            MethodParameter methodParameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        if (!(targetType instanceof Class<?> targetClass)) {
            return false;
        }
        return PostDTO.class.isAssignableFrom(targetClass);
    }

    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        if (!(body instanceof final PostDTO dto))
            return body;
        if (!(resolveCurrentRequest() instanceof final HttpServletRequest request))
            return body;

        final var remoteAddress = Stream.of(
                request.getRemoteAddr(),
                request.getHeader("X-Forwarded-For"),
                request.getHeader("x-forwarded-for"))
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("");
        final var postHash = PostHashLogic.generate(
                dto.text(),
                remoteAddress,
                request.getSession().getId());
        return new PostDTO(postHash, dto.text());
    }

    private HttpServletRequest resolveCurrentRequest() {
        try {
            var attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes sra) {
                return sra.getRequest();
            }
        } catch (Throwable t) {
        }
        return null;
    }

}
