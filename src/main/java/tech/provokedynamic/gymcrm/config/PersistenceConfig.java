package tech.provokedynamic.gymcrm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.listener.logging.OutputParameterLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.QueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.internal.DDLFormatterImpl;
import org.hibernate.tool.schema.Action;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;
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

    @Bean
    public DataSource dataSource() {
        var config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }

    @Bean
    @Primary
    @Profile("dev")
    public ProxyDataSource proxyDataSource(DataSource dataSource) {
        return ProxyDataSourceBuilder.create(dataSource)
                .name("gym-crm")
                .listener(queryLoggingListener())
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
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        var entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setPackagesToScan("tech.provokedynamic.gymcrm.entity");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setJpaDialect(new HibernateJpaDialect());

        entityManagerFactoryBean.setJpaPropertyMap(Map.of(
                AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION,
                Action.ACTION_CREATE_THEN_DROP,

                AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                new PhysicalNamingStrategySnakeCaseImpl()
        ));

        return entityManagerFactoryBean;
    }

    private SLF4JQueryLoggingListener queryLoggingListener() {
        var formatter = getQueryLogEntryCreator();

        var listener = new SLF4JQueryLoggingListener();
        listener.setLogLevel(SLF4JLogLevel.DEBUG);
        listener.setQueryLogEntryCreator(formatter);

        return listener;
    }

    private @NonNull QueryLogEntryCreator getQueryLogEntryCreator() {
        return new OutputParameterLogEntryCreator() {

            @Override
            protected String formatQuery(String query) {
                return new DDLFormatterImpl().format(query);
            }
        };
    }
}
