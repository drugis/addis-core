package org.drugis.addis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)},
        basePackages = {
                "org.drugis.addis.projects",
                "org.drugis.addis.outcomes",
                "org.drugis.addis.interventions",
                "org.drugis.addis.analyses",
                "org.drugis.addis.scenarios",
                "org.drugis.addis.security",
                "org.drugis.addis.models",
                "org.drugis.addis.problems",
                "org.drugis.addis.trialverse.service",
                "org.drugis.addis.trialverse.factory",
                "org.drugis.addis.util"
        })
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
        "org.drugis.addis.projects",
        "org.drugis.addis.outcomes",
        "org.drugis.addis.interventions",
        "org.drugis.addis.analyses",
        "org.drugis.addis.scenarios",
        "org.drugis.addis.models"
})
public class JpaRepositoryTestConfig {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:/schema.sql")
            .addScript("classpath:/test-data.sql")
            .build();
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
  }

  @Bean(name = "jtAddisCore")
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }

  @Bean
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
    return new PersistenceExceptionTranslationPostProcessor();
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(false);
    vendorAdapter.setShowSql(true);
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setJpaVendorAdapter(vendorAdapter);
    em.setPackagesToScan("org.drugis.addis.outcomes",
            "org.drugis.addis.interventions",
            "org.drugis.addis.projects",
            "org.drugis.addis.analyses",
            "org.drugis.addis.scenarios",
            "org.drugis.addis.security",
            "org.drugis.addis.models");
    em.setDataSource(dataSource());
    em.setPersistenceUnitName("addisCore");
    em.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
    em.setJpaProperties(additionalProperties());
    em.afterPropertiesSet();
    return em;
  }

  Properties additionalProperties() {
    return new Properties() {
      {
        setProperty("hibernate.hbm2ddl.auto", "update");
        setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        setProperty("hibernate.current_session_context_class", "thread");
      }
    };
  }
}
