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
package org.drugis.trialverse.security.repository.impl;

import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.UsernameAlreadyInUseException;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

  @Inject
  @Qualifier("jtTrialverse")
  private JdbcTemplate jdbcTemplate;

  private RowMapper<Account> rowMapper = new RowMapper<Account>() {
    public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Account(rs.getInt("id"), rs.getString("username"), rs.getString("firstName"), rs.getString("lastName"));
    }
  };

  @Transactional()
  public void createAccount(Account user) throws UsernameAlreadyInUseException {
    try {
      jdbcTemplate.update(
              "insert into Account (firstName, lastName, username) values (?, ?, ?)",
              user.getFirstName(), user.getLastName(), user.getUsername());
    } catch (DuplicateKeyException e) {
      throw new UsernameAlreadyInUseException(user.getUsername());
    }
  }

  public Account findAccountByUsername(String username) {
    return jdbcTemplate.queryForObject(
            "select id, username, firstName, lastName from Account where username = ?",
            rowMapper, username);
  }

  public Account findAccountById(int id) {
    return jdbcTemplate.queryForObject(
            "select id, username, firstName, lastName from Account where id = ?",
            rowMapper, id);
  }

}
