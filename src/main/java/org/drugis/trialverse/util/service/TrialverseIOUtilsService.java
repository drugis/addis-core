package org.drugis.trialverse.util.service;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by connor on 12-11-14.
 */
public interface TrialverseIOUtilsService {
  void writeResponseContentToServletResponse(HttpResponse httpResponse, HttpServletResponse httpServletResponse);

  void writeModelToServletResponse(Model model, HttpServletResponse httpServletResponse);
}
