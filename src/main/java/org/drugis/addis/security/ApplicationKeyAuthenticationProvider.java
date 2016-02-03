package org.drugis.addis.security;

import org.apache.jena.ext.com.google.common.base.Optional;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.security.TooManyAccountsException;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by connor on 9/10/15.
 */
public class ApplicationKeyAuthenticationProvider implements AuthenticationProvider {

  @Inject
  private ApiKeyRepository apiKeyRepository;

  @Inject
  private AccountRepository accountRepository;

  public ApplicationKeyAuthenticationProvider( ) {

  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    Optional<String> applicationKey = (Optional) authentication.getPrincipal();
    if (!applicationKey.isPresent() || applicationKey.get().isEmpty()) {
      throw new BadCredentialsException("Invalid applicationKey");
    }
    Account account = null;
    try {
      account = accountRepository.findAccountByActiveApplicationKey(applicationKey.get());
    } catch (TooManyAccountsException e) {
      e.printStackTrace();
      throw new BadCredentialsException("Invalid token or token expired");
    }

    if(account == null) {
      throw new BadCredentialsException("Invalid token or token expired");
    }

    ApiKey apiKey = apiKeyRepository.getKeyBySecretCode(applicationKey.get());

    GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_USER");
    return new PreAuthenticatedAuthenticationToken(account, apiKey, Arrays.asList(auth));

  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(PreAuthenticatedAuthenticationToken.class);
  }
}
