package org.drugis.trialverse.scratch.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by connor on 08/01/15.
 */

public interface ScratchService {
  void proxyPost(HttpServletRequest request, HttpServletResponse response);
}
