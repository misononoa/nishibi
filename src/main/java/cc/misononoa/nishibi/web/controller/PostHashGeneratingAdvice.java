package cc.misononoa.nishibi.web.controller;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import cc.misononoa.nishibi.util.TimeUtils;
import cc.misononoa.nishibi.web.controller.PostsController.PostDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Component
@ControllerAdvice
public class PostHashGeneratingAdvice extends RequestBodyAdviceAdapter {

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
        if (!(body instanceof PostDTO dto))
            return body;
        if (!(resolveCurrentRequest() instanceof HttpServletRequest request))
            return body;

        final var session = request.getSession();

        final var reqAddr = Stream.of(
                request.getRemoteAddr(),
                request.getHeader("X-Forwarded-For"),
                request.getHeader("x-forwarded-for")).findFirst()
                .orElse("");

        var p = "text:" + dto.text() + ";"
                + "timestamp:" + TimeUtils.nowString() + ";"
                + "remote:" + reqAddr + ";"
                + "sessionId:" + getSessionId(session).orElse("none") + ";"
                + "lastAccessed:" + getLastAccessedTime(session).orElse("none") + ";";
        return new PostDTO(
                DigestUtils.sha1Hex(p),
                dto.text());
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

    private static Optional<String> getSessionId(HttpSession session) {
        try {
            return Optional.of(session.getId());
        } catch (IllegalStateException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> getLastAccessedTime(HttpSession session) {
        try {
            long lastAccessed = session.getLastAccessedTime();
            return Optional.of(TimeUtils.epochMilliToString(lastAccessed));
        } catch (IllegalStateException ex) {
            return Optional.empty();
        }
    }

}
