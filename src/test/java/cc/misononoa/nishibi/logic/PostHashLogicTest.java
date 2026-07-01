package cc.misononoa.nishibi.logic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * 投稿ハッシュのロジックの単体テスト。純粋な関数なのでMock不要。
 */
class PostHashLogicTest {

    @Test
    void generate_returnsFortyCharLowercaseHex() {
        var hash = PostHashLogic.generate("hello", "127.0.0.1", Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(hash).matches("[0-9a-f]{40}");
    }

    @Test
    void generate_toleratesNullText() {
        var hash = PostHashLogic.generate(null, "127.0.0.1", Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(hash).matches("[0-9a-f]{40}");
    }

    @ParameterizedTest
    @MethodSource
    void extract_returnsReferencedAbbrevHashes(String text, List<String> expected) {
        assertThat(PostHashLogic.extract(text)).isEqualTo(expected);
    }

    static Stream<Arguments> extract_returnsReferencedAbbrevHashes() {
        return Stream.of(
                // 参照なし
                arguments("just some text without links", List.of()),
                // 空白区切り
                arguments("see #a1b2c3d for details", List.of("a1b2c3d")),
                // 文末($)
                arguments("look at #0123456", List.of("0123456")),
                // 日本語の句読点区切り
                arguments("#abcdef0、#1234567。", List.of("abcdef0", "1234567")),
                // '<'（HTMLタグ）区切り
                arguments("link #abcdef0<br>", List.of("abcdef0")),
                // 7文字未満は無視
                arguments("too short #abcde ", List.of()),
                // 非16進が続くと7文字揃わず無視される (#abcxyz... の 'x' は非hex)
                arguments("#abcxyz1 ", List.of()),
                // 40文字ちょうどは許容
                arguments("#0123456789abcdef0123456789abcdef01234567 ",
                        List.of("0123456789abcdef0123456789abcdef01234567")));
    }
}
