package org.drugis.trialverse.util.service;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by connor on 12-11-14.
 */
public interface TrialverseIOUtilsService {
  void writeResponseContentToServletResponse(HttpResponse httpResponse, HttpServletResponse httpServletResponse) throws IOException;

  void writeModelToServletResponse(Model model, HttpServletResponse httpServletResponse);


}
