package org.drugis.addis.security.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.UsernameAlreadyInUseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by connor on 8/13/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class AccountRepositoryTest {

  @Inject
  AccountRepository accountRepository;

  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Test
  public void testCreateAccount() throws UsernameAlreadyInUseException {

    String username = "username";
    String fistName = "firstName";
    String lastName = "lastName";
    String email = "email";
    Account accountToCreate = new Account(username, fistName, lastName, email);

    accountRepository.createAccount(accountToCreate);

    Account result = accountRepository.findAccountByUsername(username);
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(username, result.getUsername());
    assertEquals(fistName, result.getFirstName());
    assertEquals(lastName, result.getLastName());
    assertEquals(email, result.getEmail());
  }

  @Test
  public void testFindAccountByUsername() {
    String connorUserName = "1000123";
    Account result = accountRepository.findAccountByUsername(connorUserName);
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(connorUserName, result.getUsername());
    assertEquals("Connor", result.getFirstName());
    assertEquals("Bonnor", result.getLastName());
    assertEquals("connor@test.com", result.getEmail());
  }

  @Test
  public void testFindAccountById() {
    int daanUserRowId = 2;
    Account result = accountRepository.findAccountById(daanUserRowId);
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals("2000123", result.getUsername());
    assertEquals("Daan", result.getFirstName());
    assertEquals("Baan", result.getLastName());
    assertEquals("foo@bar.com", result.getEmail());
  }

  @Test(expected = DuplicateKeyException.class)
  public void testAddExtantEmailFails() {
    Account account = new Account("duplicate", "first", "last", "connor@test.com");
    accountRepository.createAccount(account);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void testNullEmailFails() {
    Account account = new Account("something", "first", "last", null);
    accountRepository.createAccount(account);
  }

}
