package org.drugis.addis.config;

import org.mockito.Mockito;
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
@ComponentScan(basePackages = {"org.drugis.addis.trialverse.repository", "org.drugis.addis.security", "org.drugis.addis.trialverse.model"}, excludeFilters = {@ComponentScan.Filter(Configuration.class)})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.drugis.addis.trialverse.repository")
public class JpaTrialverseRepositoryTestConfig {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:/trialverse-schema.sql")
            .addScript("classpath:/trialverse-test-data.sql")
            .build();
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    transactionManager.setPersistenceUnitName("trialverse");
    return transactionManager;
  }

  @Bean(name = "jtTrialverse")
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }

  @Bean(name = "jtAddisCore")
  public JdbcTemplate jdbcTemplateMock() {
    return Mockito.mock(JdbcTemplate.class);
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
    em.setPackagesToScan("org.drugis.addis.trialverse.model");
    em.setDataSource(dataSource());
    em.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
    em.setJpaProperties(additionalProperties());
    em.setPersistenceUnitName("trialverse");
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
