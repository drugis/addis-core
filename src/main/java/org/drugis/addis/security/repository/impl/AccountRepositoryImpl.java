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
package org.drugis.addis.security.repository.impl;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.security.TooManyAccountsException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

  @Inject
  @Qualifier("jtAddisCore")
  private JdbcTemplate jdbcTemplate;

  private RowMapper<Account> rowMapper = new RowMapper<Account>() {
    public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Account(rs.getInt("id"), rs.getString("username"), rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"));
    }
  };

  @Transactional("ptmAddisCore")
  public void createAccount(Account user) {
    jdbcTemplate.update(
            "insert into Account (firstName, lastName, username, email) values (?, ?, ?, ?)",
            user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
  }

  public Account findAccountByUsername(String username) {
    return jdbcTemplate.queryForObject(
            "select id, username, firstName, lastName, email from Account where username = ?",
            rowMapper, username);
  }

  public Account findAccountByEmail(String email) {
    return jdbcTemplate.queryForObject(
            "select id, username, firstName, lastName, email from Account where email = ?",
            rowMapper, email);
  }

  public Account findAccountById(int id) {
    return jdbcTemplate.queryForObject(
            "select id, username, firstName, lastName, email from Account where id = ?",
            rowMapper, id);
  }

  @Override
  public Account findAccountByActiveApplicationKey(String applicationKey) throws TooManyAccountsException {

    List<org.drugis.addis.security.Account> result = jdbcTemplate.query(
            "select id, username, firstName, lastNamefrom Account where id = (" +
                    "select accountId from ApplicationKey where secretkey = ? " +
                    "AND revocationDate > now() " +
                    "AND creationDate < now() )",
            rowMapper, applicationKey);

    if (result.size() > 1) {
      throw new TooManyAccountsException();
    }
    return result.size() == 0 ? null : result.get(0);
  }

  @Override
  public List<Account> getUsers() {
    return jdbcTemplate.query("select id, username, firstName, lastName, email from Account", rowMapper);
  }

}