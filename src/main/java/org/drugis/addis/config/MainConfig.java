/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drugis.addis.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.drugis.trialverse.util.JenaGraphMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jndi.JndiTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(excludeFilters = {@Filter(Configuration.class)}, basePackages = {
        "org.drugis.addis", "org.drugis.trialverse"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"org.drugis.addis.projects",
        "org.drugis.addis.security",
        "org.drugis.addis.covariates",
        "org.drugis.addis.interventions",
        "org.drugis.addis.scenarios",
        "org.drugis.addis.models",
        "org.drugis.addis.remarks",
        "org.drugis.trialverse.security"
})
public class MainConfig {

  private final static Logger logger = LoggerFactory.getLogger(MainConfig.class);

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient()));
    restTemplate.getMessageConverters().add(new JenaGraphMessageConverter());
    return restTemplate;
  }

  @Bean
  public HttpClient httpClient() {
    logger.info("httpClient created");
    return HttpClientBuilder
            .create()
            .setMaxConnTotal(20)
            .setMaxConnPerRoute(2)
            .build();
  }

  @Bean(name = "dsAddisCore")
  public DataSource dataSource() {
    DataSource ds;
    JndiTemplate jndi = new JndiTemplate();
    try {
      ds = (DataSource) jndi.lookup("java:/comp/env/jdbc/addiscore");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return ds;
  }

  @Bean(name = "dsPataviTask")
  public DataSource dataSourcePataviTask() {
    DataSource ds;
    JndiTemplate jndi = new JndiTemplate();
    try {
      ds = (DataSource) jndi.lookup("java:/comp/env/jdbc/patavitask");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return ds;
  }

  @Bean(name = "ptmAddisCore")
  public PlatformTransactionManager transactionManager(@Qualifier("emAddisCore") EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
  }

  @Bean(name = "jtAddisCore")
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }

  @Bean(name = "jtPataviTask")
  public JdbcTemplate jdbcTemplatePataviTask() {
    return new JdbcTemplate(dataSourcePataviTask());
  }

  @Bean(name = "petppAddisCore")
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
    return new PersistenceExceptionTranslationPostProcessor();
  }

  @Bean(name = "emAddisCore")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(false);
    vendorAdapter.setShowSql(false);
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setJpaProperties(additionalProperties());
    em.setJpaVendorAdapter(vendorAdapter);
    em.setPackagesToScan(
            "org.drugis.addis.projects",
            "org.drugis.addis.outcomes",
            "org.drugis.addis.interventions",
            "org.drugis.addis.security",
            "org.drugis.addis.analyses",
            "org.drugis.addis.scenarios",
            "org.drugis.addis.models",
            "org.drugis.addis.covariates",
            "org.drugis.addis.remarks");
    em.setDataSource(dataSource());
    em.setPersistenceUnitName("addisCore");
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
