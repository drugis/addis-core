package org.drugis.trialverse.util.service;

import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by connor on 12-11-14.
 */
public interface TrialverseIOUtilesService {
  void writeResponceContentToServletResponce(HttpResponse httpResponse, HttpServletResponse httpServletResponse);
}
