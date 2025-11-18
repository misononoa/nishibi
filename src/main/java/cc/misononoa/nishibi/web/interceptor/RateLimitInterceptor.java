package cc.misononoa.nishibi.web.interceptor;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import cc.misononoa.nishibi.web.WebConfig.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Deque<Instant>> accessMap = new ConcurrentHashMap<>();

    private final Duration window;
    private final Integer limit;

    public RateLimitInterceptor(RateLimitProperties properties) {
        this.window = Duration.ofSeconds(properties.window());
        this.limit = properties.limit();
    }

    @Override
    public boolean preHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler) {
        if (!request.getMethod().equalsIgnoreCase("POST"))
            return true;

        final var now = Instant.now();
        final var ip = request.getRemoteAddr();
        accessMap.putIfAbsent(ip, new ArrayDeque<>());
        var timestamps = accessMap.get(ip);
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(now.minus(window))) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= limit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                try (var writer = response.getWriter()) {
                    writer.write(HttpStatus.TOO_MANY_REQUESTS.toString());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return false;
            }
            timestamps.addLast(now);
        }
        return true;
    }

}
