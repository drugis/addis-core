package org.drugis.trialverse.user.controller;

import org.apache.http.entity.ContentType;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;

import java.security.Principal;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Configuration
@EnableWebMvc
public class UserControllerTest {
  private MockMvc mockMvc;
  private Principal principal = mock(Principal.class);

  @Mock
  private AccountRepository accountRepository;

  @InjectMocks
  private UserController userController;

  @Before
  public void setUp() throws Exception {
    when(principal.getName()).thenReturn("mary");
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(accountRepository);
  }

  @Test
  public void getUser() throws Exception {
    Account account = new Account("maryAcc", "mary", "jones", "foo@bar.com");
    int accountId = 3;
    when(accountRepository.findAccountById(accountId)).thenReturn(account);
    ResultActions result = mockMvc.perform(get("/users/" + accountId));
    result.andExpect(status().isOk());
    result.andExpect(content().contentType(ContentType.APPLICATION_JSON.toString()));
    result.andExpect(jsonPath("$.username", is("maryAcc")));
    result.andExpect(jsonPath("$.firstName", is("mary")));
    result.andExpect(jsonPath("$.lastName", is("jones")));
    result.andExpect(jsonPath("$.email", is("foo@bar.com")));
    verify(accountRepository).findAccountById(accountId);
  }

  @Test
  public void getAllUsers() throws Exception {
    Account account1 = new Account("maryAcc", "mary", "jones", "foo@bar.com");
    Account account2 = new Account("maryAcc2", "mary2", "jones2", "foo2@bar.com");

    when(accountRepository.getUsers()).thenReturn(Arrays.asList(account1, account2));
    ResultActions result = mockMvc.perform(get("/users"));
    result.andExpect(status().isOk());
    result.andExpect(content().contentType(ContentType.APPLICATION_JSON.toString()));
    result.andExpect(jsonPath("$", hasSize(2)));
    verify(accountRepository).getUsers();
  }

  @Test
  public void getLoggedInUser() throws Exception {
    Account account = new Account("maryAcc", "mary", "jones", "foo@bar.com");
    when(accountRepository.getAccount(principal)).thenReturn(account);
    ResultActions result = mockMvc.perform(get("/users/me")
        .principal(principal)
    );
    result.andExpect(status().isOk());
    result.andExpect(content().contentType(ContentType.APPLICATION_JSON.toString()));
    result.andExpect(jsonPath("$.username", is("maryAcc")));
    result.andExpect(jsonPath("$.firstName", is("mary")));
    result.andExpect(jsonPath("$.lastName", is("jones")));
    result.andExpect(jsonPath("$.email", is("foo@bar.com")));
    verify(accountRepository).getAccount(principal);
  }
}