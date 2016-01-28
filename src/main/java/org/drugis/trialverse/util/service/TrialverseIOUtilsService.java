package org.drugis.trialverse.util.service;


import org.apache.jena.rdf.model.Model;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by connor on 12-11-14.
 */
public interface TrialverseIOUtilsService {
  void writeContentToServletResponse(byte[] content, HttpServletResponse httpServletResponse) throws IOException;

  void writeModelToServletResponse(Model model, HttpServletResponse httpServletResponse);

  void writeModelToServletResponseJson(Model model, HttpServletResponse httpServletResponse);

}
