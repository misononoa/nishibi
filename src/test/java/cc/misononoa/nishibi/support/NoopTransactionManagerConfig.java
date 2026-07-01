package cc.misononoa.nishibi.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * DBオートコンフィグを除外したテストでは {@code PlatformTransactionManager} が存在しないため、
 * {@code @Transactional}（{@code PostService#createPost}）を成立させるための何もしないトランザクション
 * マネージャを提供する。DB接続は行わない。
 */
@TestConfiguration(proxyBeanMethods = false)
public class NoopTransactionManagerConfig {

    @Bean
    PlatformTransactionManager transactionManager() {
        return new AbstractPlatformTransactionManager() {

            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
                // no-op
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
                // no-op
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
                // no-op
            }
        };
    }
}
