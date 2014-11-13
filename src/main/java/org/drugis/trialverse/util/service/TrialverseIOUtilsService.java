package org.drugis.trialverse.util.service;

import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by connor on 12-11-14.
 */
public interface TrialverseIOUtilsService {
  void writeResponseContentToServletResponse(HttpResponse httpResponse, HttpServletResponse httpServletResponse);
}
