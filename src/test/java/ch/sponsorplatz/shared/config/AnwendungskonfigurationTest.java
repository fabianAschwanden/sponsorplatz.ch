package ch.sponsorplatz.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AnwendungskonfigurationTest.TestAnwendung.class)
@ActiveProfiles("dev")
class AnwendungskonfigurationTest {
    @Value("${spring.jpa.open-in-view}")
    private boolean openInView;

    @Value("${spring.messages.basename}")
    private String messageBasename;

    @Test
    void normalerTestErbtDieAnwendungskonfiguration() {
        assertThat(openInView).isFalse();
        assertThat(messageBasename).isEqualTo("messages");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            FlywayAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    static class TestAnwendung {
    }
}
