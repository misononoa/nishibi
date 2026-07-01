package cc.misononoa.nishibi.e2e;

import static com.codeborne.selenide.Selenide.closeWebDriver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.codeborne.selenide.Configuration;

import cc.misononoa.nishibi.repository.PostRelationRepository;
import cc.misononoa.nishibi.repository.PostRepository;
import cc.misononoa.nishibi.support.NoopTransactionManagerConfig;

/**
 * Selenideによるe2e結合テストの基底クラス。
 * <p>
 * アプリ全体をランダムポートで起動しヘッドレスChromeで操作する。ただし方針どおりDBには接続せず、
 * {@link PostRepository} / {@link PostRelationRepository} をMockitoでスタブ化する。
 * DBオートコンフィグは test プロファイルで除外し、{@code @Transactional} を成立させるために
 * {@link NoopTransactionManagerConfig} を読み込む。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(NoopTransactionManagerConfig.class)
abstract class AbstractSelenideE2ETest {

    @LocalServerPort
    protected int port;

    @MockitoBean
    protected PostRepository postRepository;

    @MockitoBean
    protected PostRelationRepository postRelationRepository;

    @BeforeAll
    static void configureSelenide() {
        var options = new ChromeOptions();
        // CI（コンテナ環境）で安定して動かすためのフラグ。
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        Configuration.browser = "chrome";
        Configuration.headless = true;
        Configuration.browserCapabilities = options;
        Configuration.browserSize = "1280x900";
        Configuration.timeout = 8000;
    }

    @BeforeEach
    void configureBaseUrl() {
        Configuration.baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void quitBrowser() {
        closeWebDriver();
    }
}
