package org.drugis.trialverse.util.service;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by connor on 12-11-14.
 */
public interface TrialverseIOUtilsService {
  void writeResponseContentToServletResponse(CloseableHttpResponse httpResponse, HttpServletResponse httpServletResponse) throws IOException;

  void writeStreamToServletResponse(InputStream inputStream, HttpServletResponse httpServletResponse);

  void writeModelToServletResponse(Model model, HttpServletResponse httpServletResponse);


}
