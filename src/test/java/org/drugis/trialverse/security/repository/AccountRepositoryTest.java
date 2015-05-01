package org.drugis.trialverse.security.repository;

import org.drugis.trialverse.config.JpaRepositoryTestConfig;
import org.drugis.trialverse.security.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 31-10-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class AccountRepositoryTest {

  @Inject
  private AccountRepository accountRepository;

  @PersistenceContext(unitName = "trialverse")
  EntityManager em;

  @Test
  public void testCreateAccount() throws Exception {
    accountRepository.createAccount("username", "firstname", "lastname");
    Account result = accountRepository.findAccountByUsername("username");
    assertEquals("username", result.getUsername());
    assertEquals("firstname", result.getFirstName());
    assertEquals("lastname", result.getLastName());
  }

  @Test
  public void testFindUserByUserNameHash() {
    Account account = accountRepository.findAccountByHash("hashedUserNameConnor");
    assertEquals("foo@bar.com", account.getUsername());
    assertEquals("Connor", account.getFirstName());
    assertEquals("Bonnor", account.getLastName());
  }

}