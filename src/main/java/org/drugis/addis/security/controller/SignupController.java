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
package org.drugis.addis.security.controller;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.SignInUtils;
import org.drugis.addis.security.UsernameAlreadyInUseException;
import org.drugis.addis.security.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;

@Controller
public class SignupController {

  final static Logger logger = LoggerFactory.getLogger(SignupController.class);

  private final AccountRepository accountRepository;
  private final ProviderSignInUtils providerSignInUtils;

  @Inject
  public SignupController(AccountRepository accountRepository,
                          ConnectionFactoryLocator connectionFactoryLocator,
                          UsersConnectionRepository connectionRepository) {
    this.accountRepository = accountRepository;
    this.providerSignInUtils = new ProviderSignInUtils(connectionFactoryLocator, connectionRepository);
  }

  @RequestMapping(value = "/signup", method = RequestMethod.GET)
  public String signupForm(WebRequest request) {
    logger.info("sign up for new account");
    Connection<?> connection = providerSignInUtils.getConnectionFromSession(request);
    if (connection != null) {
      logger.info(" C o N n e c t i o n  NOT null");
      UserProfile profile = connection.fetchUserProfile();
      logger.info("profile fetched. name: " + profile.getName() + " username: " + profile.getUsername() + " email: " + profile.getEmail());
      Account account = createAccount(profile);
      if (account != null) {
        SignInUtils.signin(account.getUsername());
        providerSignInUtils.doPostSignUp(account.getUsername(), request);
        return "redirect:/";
      } else {
        logger.error("No account available ");
        return null;
      }
    } else {
      logger.error("No connection available ");
      return null;
    }
  }

  private Account createAccount(UserProfile profile) {
		try {
			Account account = new Account(profile.getUsername(), profile.getFirstName(), profile.getLastName(), profile.getEmail());
			accountRepository.createAccount(account);
      logger.info("new account created");
			return account;
		} catch (UsernameAlreadyInUseException e) {
			logger.error("UsernameAlreadyInUseException");
      return null;
		}
	}
}
