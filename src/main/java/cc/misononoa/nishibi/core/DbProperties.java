package cc.misononoa.nishibi.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nishibi.db")
public record DbProperties(
        String host,
        int port,
        String name,
        String username,
        String password) {
}
