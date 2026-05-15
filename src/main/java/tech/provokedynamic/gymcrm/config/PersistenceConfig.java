package tech.provokedynamic.gymcrm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceConfiguration;
import net.ttddyy.dsproxy.listener.logging.OutputParameterLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.engine.jdbc.internal.DDLFormatterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.entity.User;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Configuration(proxyBeanMethods = false)
public class PersistenceConfig {
    private static final Logger log = LoggerFactory.getLogger(PersistenceConfig.class);

    @Value("${db.user}")
    private String user;

    @Value("${db.password}")
    private String password;

    @Value("${db.url}")
    private String url;

    @Value("${db.ddl-auto}")
    private String ddlAuto;

    @Bean
    public DataSource dataSource() {
        var config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    @Profile("dev")
    public ProxyDataSource proxyDataSource(DataSource dataSource) {
        var listener = getSlf4JQueryLoggingListener();

        return ProxyDataSourceBuilder.create(dataSource)
                .name("gym-crm")
                .listener(listener)
                .countQuery()
                .logSlowQueryBySlf4j(500, TimeUnit.MILLISECONDS, SLF4JLogLevel.WARN)
                .afterQuery((executionInfo, _) -> {
                    if (executionInfo.getThrowable() != null) {
                        log.error("Query failed: {}", executionInfo.getThrowable().getMessage());
                    }
                })
                .build();
    }

    @Bean
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        var configuration = new PersistenceConfiguration("tech.provokedynamic.gymcrm")
                .property(JdbcSettings.JAKARTA_NON_JTA_DATASOURCE, dataSource)
                .property(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, ddlAuto)
                .property(AvailableSettings.PHYSICAL_NAMING_STRATEGY, new PhysicalNamingStrategySnakeCaseImpl())
                .managedClass(User.class)
                .managedClass(TrainingType.class);
        return Persistence.createEntityManagerFactory(configuration);
    }

    private SLF4JQueryLoggingListener getSlf4JQueryLoggingListener() {
        var formatter = new OutputParameterLogEntryCreator() {
            @Override
            protected String formatQuery(String query) {
                return new DDLFormatterImpl().format(query);
            }
        };
        formatter.setMultiline(true);

        var listener = new SLF4JQueryLoggingListener();
        listener.setLogLevel(SLF4JLogLevel.DEBUG);
        listener.setQueryLogEntryCreator(formatter);
        return listener;
    }
}
