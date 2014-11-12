package org.drugis.trialverse.util.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.util.service.TrialverseIOUtilesService;
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
public class TrialverseIOUtilsServiceImpl implements TrialverseIOUtilesService {

  final static Logger logger = LoggerFactory.getLogger(TrialverseIOUtilsServiceImpl.class);

  @Override
  public void writeResponceContentToServletResponce(HttpResponse httpResponse, HttpServletResponse httpServletResponse) {
    httpServletResponse.setHeader("Content-Type", "application/json");
    try (InputStream inputStream = httpResponse.getEntity().getContent();
         ServletOutputStream outputStream = httpServletResponse.getOutputStream();
    ) {
      IOUtils.copy(inputStream, outputStream);
      inputStream.close();
      outputStream.close();
    } catch (IOException e) {
      logger.error(e.toString());
    }
  }
}
