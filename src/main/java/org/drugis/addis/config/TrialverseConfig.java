package org.drugis.addis.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by connor on 2/27/14.
 */
@Configuration
@ComponentScan(basePackages = "org.drugis.addis.trialverse", excludeFilters = {@ComponentScan.Filter(Configuration.class)})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"org.drugis.addis.trialverse"})
public class TrialverseConfig {
  @Bean(name = "dsTrialverse")
  public DataSource dataSource() {
    DataSource ds;
    JndiTemplate jndi = new JndiTemplate();
    try {
      ds = (DataSource) jndi.lookup("java:/comp/env/jdbc/trialverse");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return ds;
  }

  @Bean(name = "ptmTrialverse")
  public PlatformTransactionManager transactionManager(@Qualifier("emTrialverse") EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
  }

  @Bean(name = "jdbcTrialverse")
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }

  @Bean(name = "petppTrialverse")
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
    return new PersistenceExceptionTranslationPostProcessor();
  }

  @Bean(name = "emTrialverse")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(false);
    vendorAdapter.setShowSql(true);
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setJpaProperties(additionalProperties());
    em.setJpaVendorAdapter(vendorAdapter);
    em.setPackagesToScan("org.drugis.addis.trialverse", "org.drugis.addis.trialverse.model");
    em.setPersistenceUnitName("trialverse");
    em.setDataSource(dataSource());
    em.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
    em.afterPropertiesSet();
    return em;
  }

  Properties additionalProperties() {
    return new Properties() {
      {
        setProperty("hibernate.hbm2ddl.auto", "validate");
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        setProperty("hibernate.current_session_context_class", "thread");
      }
    };
  }

}
