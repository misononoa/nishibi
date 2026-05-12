package cc.misononoa.nishibi.core.web.model;

import java.time.Instant;
import java.time.ZoneId;

public record RequestInfo(
        String remoteAddr,
        Instant requestTime,
        ZoneId timeZone) {

}
