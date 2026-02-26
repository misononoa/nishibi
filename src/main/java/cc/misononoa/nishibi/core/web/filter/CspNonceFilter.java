package cc.misononoa.nishibi.core.web.filter;

import java.io.IOException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

@Component
public class CspNonceFilter extends OncePerRequestFilter {

    public static final String NONCE_ATTR_NAME = "_nonce";
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final var nonce = generateNonce();
        request.setAttribute(NONCE_ATTR_NAME, nonce);
        filterChain.doFilter(request, new CspNonceResponseWrapper(response, nonce));
    }

    private String generateNonce() {
        var nonceBytes = new byte[32];
        secureRandom.nextBytes(nonceBytes);
        return Base64.encodeBase64String(nonceBytes);
    }

    private class CspNonceResponseWrapper extends HttpServletResponseWrapper {

        private static final String CSP_HEADER_KEY = "Content-Security-Policy";

        private final String nonceVal;

        CspNonceResponseWrapper(HttpServletResponse response, String nonceVal) {
            super(response);
            this.nonceVal = nonceVal;
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, evalHeaderValue(name, value));
        }

        @Override
        public void addHeader(String name, String value) {
            super.setHeader(name, evalHeaderValue(name, value));
        }

        private String evalHeaderValue(String name, String value) {
            if (CSP_HEADER_KEY.equals(name) && StringUtils.isNotBlank(value)) {
                return Strings.CS.replace(value, "{nonce}", nonceVal);
            } else {
                return value;
            }
        }
    }

    @ControllerAdvice(basePackages = "cc.misononoa.nishibi.web")
    public static class CspNonceAdvice {

        @ModelAttribute
        public void addAttributes(Model model, HttpServletRequest request) {
            model.addAttribute(CspNonceFilter.NONCE_ATTR_NAME, request.getAttribute(CspNonceFilter.NONCE_ATTR_NAME));
        }

    }

}
