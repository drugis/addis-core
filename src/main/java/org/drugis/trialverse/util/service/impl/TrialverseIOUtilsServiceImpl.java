package org.drugis.trialverse.util.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by connor on 12-11-14.
 */
@Service
public class TrialverseIOUtilsServiceImpl implements TrialverseIOUtilsService {

  private final static Logger logger = LoggerFactory.getLogger(TrialverseIOUtilsServiceImpl.class);

  @Override
  public void writeContentToServletResponse(byte[] content, HttpServletResponse httpServletResponse) throws IOException {
    try (ServletOutputStream outputStream = httpServletResponse.getOutputStream()
    ) {
      logger.trace("write output to client request");
      IOUtils.copy(new ByteArrayInputStream(content), outputStream);
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

  @Override
  public void writeModelToServletResponseJson(Model model, HttpServletResponse httpServletResponse) {
    try (ServletOutputStream outputStream = httpServletResponse.getOutputStream()) {
      model.write(outputStream, "JSON-LD"); // RDFDataMgr.write causes broken pipe in mocha test
    } catch (IOException e) {
      logger.error("Error writing jena model response to client response");
      logger.error(e.toString());
    }

  }
}
