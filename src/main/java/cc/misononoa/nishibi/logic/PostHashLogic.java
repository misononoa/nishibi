package cc.misononoa.nishibi.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import cc.misononoa.nishibi.core.util.TimeUtils;

public class PostHashLogic {

    private PostHashLogic() {
    }

    public static String generate(
            String text,
            String remoteAddress,
            String sessionId) {
        var context = "text:" + StringUtils.defaultString(text) + ";"
                + "timestamp:" + TimeUtils.nowString() + ";"
                + "remote:" + remoteAddress + ";"
                + "sessionId:" + sessionId + ";";
        return DigestUtils.sha1Hex(context);
    }

    private static final Pattern POSTLINK_PATTERN = Pattern.compile(
            "#([0-9a-f]{7,40})(?=\\s|<|$|[、。！？])");

    public static Matcher getMatcher(String text) {
        return POSTLINK_PATTERN.matcher(text);
    }

    public static List<String> extract(String text) {
        var result = new ArrayList<String>();
        var matcher = getMatcher(text);
        while (matcher.find()) {
            if (matcher.group(1) instanceof String abbrevHash)
                result.add(abbrevHash);
        }
        return Collections.unmodifiableList(result);
    }

}
