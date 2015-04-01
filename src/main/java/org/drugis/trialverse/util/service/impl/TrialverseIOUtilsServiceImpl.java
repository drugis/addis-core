package org.drugis.trialverse.util.service.impl;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.io.IOUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by connor on 12-11-14.
 */
@Service
public class TrialverseIOUtilsServiceImpl implements TrialverseIOUtilsService {

  final static Logger logger = LoggerFactory.getLogger(TrialverseIOUtilsServiceImpl.class);

  @Override
  public void writeResponseContentToServletResponse(HttpResponse httpResponse, HttpServletResponse httpServletResponse) throws IOException {
    try (InputStream inputStream = httpResponse.getEntity().getContent();
         ServletOutputStream outputStream = httpServletResponse.getOutputStream()
    ) {
      logger.trace("write output to client request");
      IOUtils.copy(inputStream, outputStream);
      logger.trace("consume version store responce");
      EntityUtils.consume(httpResponse.getEntity());
    } catch (IOException e) {
      logger.error("Error writing jena response to client response");
      logger.error(e.toString());
    } finally {
      logger.trace("close version store responce");
      try {
        ((CloseableHttpResponse) httpResponse).close();
      } catch (Exception e) {
        logger.error("Error while closing");
        throw e;
      }
    }
  }

  @Override
  public void writeModelToServletResponse(Model model, HttpServletResponse httpServletResponse) {
    try (ServletOutputStream outputStream = httpServletResponse.getOutputStream()) {
      model.write(outputStream, "TURTLE"); // RDFDataMgr.write causes broken pipe in mocha test
    } catch (IOException e) {
      logger.error("Error writing jena model response to client response");
      logger.error(e.toString());
    }

  }
}
