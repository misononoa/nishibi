package cc.misononoa.nishibi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import cc.misononoa.nishibi.repository.PostRelationRepository;
import cc.misononoa.nishibi.repository.PostRepository;
import cc.misononoa.nishibi.support.NoopTransactionManagerConfig;

/**
 * コンテキストロードのスモークテスト。方針どおりDBには接続せず、Repositoryをモック化して起動する。
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(NoopTransactionManagerConfig.class)
class NishibiBbsApplicationTests {

	@MockitoBean
	private PostRepository postRepository;

	@MockitoBean
	private PostRelationRepository postRelationRepository;

	@Test
	void contextLoads() {
	}

}
