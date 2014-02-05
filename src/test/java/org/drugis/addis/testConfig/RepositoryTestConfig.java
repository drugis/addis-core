package org.drugis.addis.testConfig;

import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

@Configuration
@ComponentScan(basePackages = {"org.drugis.addis.security.repository"})
public class RepositoryTestConfig {


  @Bean
  public DataSource dataSource() {
    JdbcDatabaseTester jdbcDatabaseTester;
    try {
      jdbcDatabaseTester = new JdbcDatabaseTester("org.hsqldb.jdbcDriver",
              "jdbc:hsqldb:sample", "sa", "");
      IDatabaseConnection conn = jdbcDatabaseTester.getConnection();
      conn.getConnection();
      DataSource
      return new
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
  }

  @Bean
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }
}
