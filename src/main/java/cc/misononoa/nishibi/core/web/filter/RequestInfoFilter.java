package cc.misononoa.nishibi.core.web.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.IpAddressValidator;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import cc.misononoa.nishibi.core.web.model.RequestInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestInfoFilter extends OncePerRequestFilter {

    private final RequestInfo requestInfo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        requestInfo.setRemoteAddr(getRemoteAddr(request));
        requestInfo.setRequestTime(Instant.now());
        requestInfo.setTimeZone(ZoneId.systemDefault());
        filterChain.doFilter(request, response);
    }

    private String getRemoteAddr(HttpServletRequest request) {
        if (request.getHeader("X-Forwarded-For") instanceof String xForwardedFor) {
            var forwardedForIp = Stream.of(StringUtils.split(xForwardedFor, ','))
                    .filter(this::isAcceptable)
                    .findFirst();
            if (forwardedForIp.isPresent()) {
                return forwardedForIp.get();
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isAcceptable(String addrStr) {
        try {
            InetAddress.ofLiteral(addrStr);
            return true;
        } catch (IllegalArgumentException _) {
        }
        return false;
    }

}
