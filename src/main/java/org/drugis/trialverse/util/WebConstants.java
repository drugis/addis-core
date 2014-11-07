package org.drugis.trialverse.util;

import org.springframework.http.MediaType;

import java.nio.charset.Charset;

/**
 * Created by connor on 2/12/14.
 */
public class WebConstants {
  public final static MediaType APPLICATION_JSON_UTF8 = new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));
  public final static String APPLICATION_JSON_UTF8_VALUE = "application/json; charset=UTF-8";
}
