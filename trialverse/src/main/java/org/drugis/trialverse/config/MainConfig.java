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
package org.drugis.trialverse.config;

import org.apache.http.client.HttpClient;

import org.apache.http.impl.client.HttpClientBuilder;
import org.drugis.trialverse.util.JenaGraphMessageConverter;
import org.drugis.trialverse.util.WebConstants;
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

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(excludeFilters = {@Filter(Configuration.class)}, basePackages = {
        "org.drugis.trialverse"})
@EnableJpaRepositories(basePackages = {"org.drugis.trialverse.security"})
public class MainConfig {

  private final static Logger logger = LoggerFactory.getLogger(MainConfig.class);

  // load environment variables on deploy
  @Inject
  WebConstants webConstants;

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

  @Bean
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


  @Bean(name = "jtTrialverse")
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }

  @Bean(name = "petppTrialverse")
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
    return new PersistenceExceptionTranslationPostProcessor();
  }



}
