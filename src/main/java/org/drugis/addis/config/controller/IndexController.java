package org.drugis.addis.config.controller;

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

import org.drugis.addis.security.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class IndexController {

  final static Logger logger = LoggerFactory.getLogger(IndexController.class);

  @Inject
  private Provider<ConnectionRepository> connectionRepositoryProvider;

  @Inject
  private AccountRepository accountRepository;

  @RequestMapping("/")
  public String index(Principal currentUser, Model model, HttpServletRequest request) {
    model.addAttribute("connectionsToProviders", getConnectionRepository().findAllConnections());
    try {
      if (currentUser != null)  {
        accountRepository.findAccountByUsername(currentUser.getName());
      }
    } catch (org.springframework.dao.EmptyResultDataAccessException e) {
      request.getSession().invalidate();
    }
    return "index.html";
  }

  private ConnectionRepository getConnectionRepository() {
    return connectionRepositoryProvider.get();
  }
}
