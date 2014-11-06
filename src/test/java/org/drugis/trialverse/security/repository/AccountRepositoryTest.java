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
    Account account = new Account("username", "firstname", "lastname");
    accountRepository.createAccount(account);
    Account result = accountRepository.findAccountByUsername("username");
    assertEquals("username", result.getUsername());
    assertEquals("firstname", result.getFirstName());
    assertEquals("lastname", result.getLastName());
  }

}