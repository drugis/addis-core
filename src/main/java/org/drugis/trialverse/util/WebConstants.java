package org.drugis.trialverse.util;

import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * Created by connor on 2/12/14.
 */
@Component
public class WebConstants {
  private final static MediaType APPLICATION_JSON_UTF8 = new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  private final static String APPLICATION_JSON_UTF8_VALUE = "application/json; charset=UTF-8";
  private final static String TRIPLESTORE_BASE_URI = loadSystemEnvTripleStoreBaseURI();
  private final static String TRIPLESTORE_DATA_URI = TRIPLESTORE_BASE_URI + "/current";

  public static final String VERSION_PATH = "versions/";
  public static final String QUERY_ENDPOINT = "/query";
  public static final String HISTORY_ENDPOINT = "/history";
  public static final String DATA_ENDPOINT = "/data";
  public static final String QUERY_PARAM_QUERY = "query";
  public static final String QUERY_STRING_DEFAULT_GRAPH = "?default";

  public static final String X_EVENT_SOURCE_VERSION = "X-EventSource-Version";
  public static final String EVENT_SOURCE_TITLE_HEADER = "X-EventSource-Title";
  public static final String EVENT_SOURCE_DESCRIPTION_HEADER = "X-EventSource-Description";
  public static final String EVENT_SOURCE_CREATOR_HEADER = "X-EventSource-Creator";
  public static final String X_ACCEPT_EVENT_SOURCE_VERSION = "X-Accept-EventSource-Version";

  public static final String COMMIT_TITLE_PARAM = "commitTitle";
  public static final String COMMIT_DESCRIPTION_PARAM = "commitDescription";

  private static String loadSystemEnvTripleStoreBaseURI() {
    String tripleStoreBaseURI = System.getenv("TRIPLESTORE_BASE_URI");
    if (tripleStoreBaseURI == null || tripleStoreBaseURI.isEmpty()) {
      LoggerFactory
              .getLogger(WebConstants.class)
              .error("Cannot start server, no TRIPLESTORE_BASE_URI environment variable found");
      System.exit(-1);
    }
    return tripleStoreBaseURI;
  }

  public String getTriplestoreBaseUri() {
    return TRIPLESTORE_BASE_URI;
  }

  public String getTriplestoreDataUri() {
    return TRIPLESTORE_DATA_URI;
  }

  public static MediaType getApplicationJsonUtf8() {
    return APPLICATION_JSON_UTF8;
  }

  public static String getApplicationJsonUtf8Value() {
    return APPLICATION_JSON_UTF8_VALUE;
  }

  public String buildVersionUri(String versionUuid) {
    return getTriplestoreBaseUri() + VERSION_PATH + versionUuid;
  }
}

