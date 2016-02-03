package org.drugis.trialverse.security;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.social.security.SocialUserDetails;

import java.security.Principal;

/**
 * Created by daan on 16-9-15.
 */
public class TrialversePrincipal {

  private ApiKey apiKey;
  private String userName;
  final static Logger logger = LoggerFactory.getLogger(TrialversePrincipal.class);


  public TrialversePrincipal(Principal principal) {
    if (principal instanceof PreAuthenticatedAuthenticationToken) {
      PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken = (PreAuthenticatedAuthenticationToken) principal;
      this.apiKey = (ApiKey) preAuthenticatedAuthenticationToken.getCredentials();
      this.userName = ((Account) preAuthenticatedAuthenticationToken.getPrincipal()).getUsername();
    } else if(principal instanceof SocialAuthenticationToken) {
      SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) principal;
      SocialUserDetails socialUserDetails = (SocialUserDetails) socialAuthenticationToken.getPrincipal();
      this.userName = socialUserDetails.getUsername();
    } else {
      logger.error("trying to create Trialverse principal with unknown authentication class");
      throw new ClassCastException("unknown principal type, should be either PreAuthenticatedAuthenticationToken or SocialAuthenticationToken type");
    }
  }

  public boolean hasApiKey() {
    return this.apiKey != null;
  }

  public ApiKey getApiKey() {
    return apiKey;
  }

  public String getUserName() {
    return userName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TrialversePrincipal that = (TrialversePrincipal) o;

    if (apiKey != null ? !apiKey.equals(that.apiKey) : that.apiKey != null) return false;
    return userName.equals(that.userName);

  }

  @Override
  public int hashCode() {
    int result = apiKey != null ? apiKey.hashCode() : 0;
    result = 31 * result + userName.hashCode();
    return result;
  }
}
