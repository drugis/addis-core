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

import org.apache.commons.lang.StringUtils;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class IndexController {

  final static Logger logger = LoggerFactory.getLogger(IndexController.class);

  private final static String DEFAULT_PATAVI_MCDA_WS_URI = "wss://patavi.drugis.org/ws";
  private final static String ADDIS_CORE_PATAVI_MCDA_WS_URI = getPataviMcdaWsUri();

  @Inject
  private Provider<ConnectionRepository> connectionRepositoryProvider;

  @Inject
  private AccountRepository accountRepository;

  private static String getPataviMcdaWsUri() {
    try {
      String uri;
      String envUri = System.getenv("ADDIS_CORE_PATAVI_MCDA_WS_URI");
      if (envUri != null && !envUri.isEmpty()) {
        uri = envUri;
      } else {
        uri = DEFAULT_PATAVI_MCDA_WS_URI;
      }
      logger.info("PATAVI_MCDA_WS_URI: " + uri);
      return uri;
    } catch (Exception e) {
      logger.error("can not find env variable PATAVI_MCDA_WS_URI fallback to using DEFAULT_PATAVI_MCDA_WS_URI: " + DEFAULT_PATAVI_MCDA_WS_URI);
      return DEFAULT_PATAVI_MCDA_WS_URI;
    }
  }

  @RequestMapping("/")
  public String index(Principal currentUser, Model model, HttpServletRequest request) {
    model.addAttribute("connectionsToProviders", getConnectionRepository().findAllConnections());
    try {
      if (currentUser == null) {
        return "redirect:/signin";
      } else {
        Account account = accountRepository.findAccountByUsername(currentUser.getName());
        model.addAttribute(account);
        if (StringUtils.isNotEmpty(account.getEmail())) {
          model.addAttribute("userEmail", account.getEmail());
          model.addAttribute("id", account.getId());
          String md5String = DigestUtils.md5DigestAsHex(account.getEmail().getBytes());
          model.addAttribute("userMD5", md5String); // user email MD5 hash needed to retrieve gravatar image
          model.addAttribute("userNameHash", md5String); // user email MD5 hash needed to retrieve gravatar image
        }

        model.addAttribute("pataviMcdaWsUri", ADDIS_CORE_PATAVI_MCDA_WS_URI);
      }
    } catch (org.springframework.dao.EmptyResultDataAccessException e) {
      request.getSession().invalidate();
      return "redirect:/signin";
    }
    return "index";
  }

  private ConnectionRepository getConnectionRepository() {
    return connectionRepositoryProvider.get();
  }
}
