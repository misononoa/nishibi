package cc.misononoa.nishibi.core.web.filter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import cc.misononoa.nishibi.core.web.model.RequestInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestInfoFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var info = new RequestInfo(request.getRemoteAddr(), Instant.now(), ZoneId.systemDefault());
        request.setAttribute("requestInfo", info);
        filterChain.doFilter(request, response);
    }

}
