package org.drugis.trialverse.security;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.social.security.SocialUser;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by connor on 11/26/15.
 */
public class TrialverseprincipalTest {

  @Test
  public void testContructorWithSocialPrincipal () {
    final String userId = "some user id";
    Connection connection = mock(Connection.class);
    ConnectionData connectionData = mock(ConnectionData.class);
    when(connectionData.getProviderId()).thenReturn("providerId");
    when(connection.createData()).thenReturn(connectionData);
    Principal principal = new SocialAuthenticationToken(connection, new SocialUser(userId, "password", Collections.emptyList()), null, null);
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(principal);
    assertEquals(userId, trialversePrincipal.getUserName());
    assertNull(trialversePrincipal.getApiKey());
  }

  @Test
  public void testContructorWithApiKeyPrincipal () {
    String userId = "some user id";
    ApiKey apiKey = new ApiKey(1, "key", 1, "appName", new Date(1L), new Date(1L));
    Account account = new Account(userId, "first", "last", "hash");
    GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_USER");
    PreAuthenticatedAuthenticationToken token =  new PreAuthenticatedAuthenticationToken(account, apiKey, Arrays.asList(auth));
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(token);
    assertEquals(userId, trialversePrincipal.getUserName());
    assertEquals(apiKey, trialversePrincipal.getApiKey());
  }

  @Test(expected = ClassCastException.class)
  public void testContructorWithUnknownPrincipal () {
    Principal principal = new TestingAuthenticationToken(new Object(), new Object());
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(principal);
  }
}
