package cc.misononoa.nishibi.core.web.model;

import java.time.Instant;
import java.time.ZoneId;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.Data;

@Data
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class RequestInfo {

    private String remoteAddr;

    private Instant requestTime;

    private ZoneId timeZone;

}
