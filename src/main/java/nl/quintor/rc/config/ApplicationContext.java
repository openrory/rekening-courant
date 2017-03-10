package nl.quintor.rc.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import liquibase.integration.spring.SpringLiquibase;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.Collections;
import java.util.Properties;

import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;

/**
 * Created by marcel on 21-1-2015.
 */
@Configuration
@ComponentScan(basePackages = "nl.quintor.rc")
@PropertySource(value = {"classpath:config/rc-config.properties", "classpath:config/rc-config.${spring.profiles.active}.properties"})
@EnableAspectJAutoProxy
@EnableCaching
public class ApplicationContext {
    @Autowired
    private Environment env;

    @Autowired
    private XADataSource xaDataSource;

    @Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
    @DependsOn("xaDataSource")
    public DataSource dataSource() {
        AtomikosDataSourceBean ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName("RC_DB_DATASOURCE");
        ds.setXaDataSource(xaDataSource);
        ds.getXaProperties().setProperty("url", env.getProperty("jdbc.dataSource.url"));
        ds.getXaProperties().setProperty("user", env.getProperty("jdbc.dataSource.username"));
        ds.getXaProperties().setProperty("password", env.getProperty("jdbc.dataSource.password"));
        return ds;
    }

    @Bean
    @DependsOn("dataSource")
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource());
        liquibase.setChangeLog("classpath:db/changelog_master.xml");
        return liquibase;
    }

    @Bean
    public SimpleCacheManager simpleCacheManager() {
        final SimpleCacheManager cacheManager = new SimpleCacheManager();
        final Cache cache = new ConcurrentMapCache("klanten");
        cacheManager.setCaches(Collections.singleton(cache));
        return cacheManager;
    }

    @DependsOn("transactionManager")
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        vendorAdapter.setGenerateDdl(true);

        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setPackagesToScan("nl.quintor.rc.model");
        em.setDataSource(dataSource());
        em.setPersistenceUnitName("RC_Unit");
        em.setJpaProperties(additionalProperties());
        em.setJpaVendorAdapter(vendorAdapter);
        return em;
    }

    private Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", MySQL5InnoDBDialect.class.getName());
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        properties.setProperty("hibernate.jdbc.batch_size", "50");
        properties.setProperty("javax.persistence.transactionType", JTA.name());
        properties.setProperty("hibernate.transaction.jta.platform", AtomikosJtaPlatform.class.getName());
        return properties;
    }
}