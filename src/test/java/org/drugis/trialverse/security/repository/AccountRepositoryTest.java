package org.drugis.trialverse.security.repository;

import org.drugis.trialverse.config.JpaRepositoryTestConfig;
import org.drugis.trialverse.security.Account;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by connor on 31-10-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class AccountRepositoryTest {

  @Inject
  private AccountRepository accountRepository;

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
    Account account = accountRepository.findAccountByHash("userNameHashConnor");
    assertEquals("foo@bar.com", account.getUsername());
    assertEquals("Connor", account.getFirstName());
    assertEquals("Bonnor", account.getLastName());
  }

  @Test
  public void testGetUsers() {
    List<Account> accounts = accountRepository.getUsers();
   assertEquals(2, accounts.size());
  }

  @Test
  public void testFindAccountByActiveApplicationKey() throws Exception {
    String applicationKey = "this aint no key";
    Account account = accountRepository.findAccountByActiveApplicationKey(applicationKey);
    assertNull(account);
  }

  @Test
  public void testFindAccountByActiveApplicationKeyExpectResult() throws Exception {
    String applicationKey = "supersecretkey";
    Account expectedAccount = accountRepository.findAccountById(1);
    Account account = accountRepository.findAccountByActiveApplicationKey(applicationKey);
    assertEquals(expectedAccount, account);
  }

}