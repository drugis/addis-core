package org.drugis.trialverse.scratch.service;

import org.apache.http.Header;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by connor on 08/01/15.
 */

public interface ScratchService {
  int update(byte[] content, String queryString, Header contentTypeHeader) throws IOException;

  /**
   *  returns response status code
   */
  int setData(byte[] content, String queryString, Header contentTypeHeader) throws IOException ;

  byte [] query(byte[] requestContent, String queryString, Header acceptHeader, Header contentTypeHeader) throws IOException;

  byte[] get(String queryString, Header acceptHeader) throws IOException;

}
