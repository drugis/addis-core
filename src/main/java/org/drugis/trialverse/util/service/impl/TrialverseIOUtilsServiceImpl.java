package org.drugis.trialverse.util.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
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
  public void writeResponseContentToServletResponse(HttpResponse httpResponse, HttpServletResponse httpServletResponse) {
    try (InputStream inputStream = httpResponse.getEntity().getContent();
         ServletOutputStream outputStream = httpServletResponse.getOutputStream()
    ) {
      IOUtils.copy(inputStream, outputStream);
      inputStream.close();
      outputStream.close();
    } catch (IOException e) {
      logger.error("Error writing jena response to client request");
      logger.error(e.toString());
    }

    Integer statusCode = httpResponse.getStatusLine().getStatusCode();
    httpServletResponse.setStatus(statusCode);
  }
}
