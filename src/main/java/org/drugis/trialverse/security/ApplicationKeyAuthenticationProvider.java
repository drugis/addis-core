package org.drugis.trialverse.security;

import org.apache.jena.ext.com.google.common.base.Optional;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * Created by connor on 9/10/15.
 */
public class ApplicationKeyAuthenticationProvider implements AuthenticationProvider {

  private AccountRepository accountRepository;

  public ApplicationKeyAuthenticationProvider(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
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

    PreAuthenticatedAuthenticationToken token= new PreAuthenticatedAuthenticationToken(account, null);
    token.setAuthenticated(true);
    return token;

  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(PreAuthenticatedAuthenticationToken.class);
  }
}
