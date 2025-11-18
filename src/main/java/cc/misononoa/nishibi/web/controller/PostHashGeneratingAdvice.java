package cc.misononoa.nishibi.web.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import cc.misononoa.nishibi.common.util.TimeUtils;
import cc.misononoa.nishibi.web.controller.PostsController.PostDTO;
import jakarta.servlet.http.HttpServletRequest;

@Component
@ControllerAdvice
public class PostHashGeneratingAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (!(targetType instanceof Class<?> targetClass)) {
            return false;
        }
        return PostDTO.class.isAssignableFrom(targetClass);
    }

    @Override
    public @NonNull Object afterBodyRead(@NonNull Object body, @NonNull HttpInputMessage inputMessage,
            @NonNull MethodParameter parameter, @NonNull Type targetType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        if (!(body instanceof PostDTO dto)
                || !(resolveCurrentRequest() instanceof HttpServletRequest servletRequest)) {
            return body;
        }
        var payload = new StringBuilder();
        payload.append(format("text", dto.getText()));
        payload.append(format("server-timestamp", TimeUtils.nowString()));
        payload.append(extractRequestTimestamp(servletRequest));
        payload.append(extractRequestAddrs(servletRequest));
        payload.append(extractSessionIdentifier(servletRequest));

        System.out.println(payload.toString());
        dto.setPostHash(DigestUtils.sha1Hex(payload.toString()));

        return dto;
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

    private String extractRequestTimestamp(HttpServletRequest request) {
        var timestamp = StringUtils.defaultIfBlank(request.getHeader("X-Request-Timestamp"), request.getHeader("date"));
        return format("request-timestamp", StringUtils.defaultIfBlank(timestamp, "none"));
    }

    private String extractRequestAddrs(HttpServletRequest request) {
        var addrs = new ArrayList<String>();
        addrs.add(request.getRemoteHost());
        var xForwardedFor = Stream.of("X-Forwarded-For")
                .flatMap(s -> Stream.of(s, s.toLowerCase()))
                .map(request::getHeader)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse("");
        addrs.addAll(List.of(xForwardedFor.split(",")));
        return format("remote-addresses", addrs);
    }

    private String extractSessionIdentifier(HttpServletRequest request) {
        var session = request.getSession();
        try {
            return format("session-identifier", session.getId(),
                    TimeUtils.epochMilliToString(session.getCreationTime()),
                    TimeUtils.epochMilliToString(session.getLastAccessedTime()));
        } catch (RuntimeException e) {
            return "session-identifier:null;";
        }
    }

    private String format(String paramName, String... values) {
        return format(paramName, List.of(values));
    }

    private String format(String paramName, Iterable<String> values) {
        final var joiner = new StringJoiner("|", paramName + ":", ";");
        for (var v : values) {
            v = StringUtils.trim(v);
            if (StringUtils.isEmpty(v))
                continue;
            joiner.add(v);
        }
        return joiner.toString();
    }

}
