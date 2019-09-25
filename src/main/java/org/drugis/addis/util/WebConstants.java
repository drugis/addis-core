package org.drugis.addis.util;

import com.fasterxml.jackson.core.Version;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.riot.WebContent;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

/**
 * Created by connor on 2/12/14.
 */
@Component
public class WebConstants {
  public static final Version SCHEMA_VERSION = new Version(1, 1, 0, null, null, null);
  private final static MediaType APPLICATION_JSON_UTF8 = new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  public static final String APPLICATION_JSON_UTF8_VALUE = "application/json; charset=UTF-8";
  public static final String APPLICATION_SPARQL_RESULTS_JSON = "application/sparql-results+json";
  public static final String JSON_LD = "application/ld+json";
  public static final String TURTLE = "text/turtle";
  public static final String TRIG = "text/trig";

  public static final String API_KEY_PREFIX = "https://trialverse.org/apikeys/";
  public static final String VERSION_PATH = "versions/";
  public static final String DATASET_PATH = "datasets/";
  public static final String QUERY_ENDPOINT = "/query";
  public static final String HISTORY_ENDPOINT = "/history";
  public static final String DATA_ENDPOINT = "/data";
  public static final String QUERY_PARAM_QUERY = "query";

  public static final String COPY_OF_QUERY_PARAM = "copyOf";
  public static final String GRAPH_QUERY_PARAM = "graph";
  public static final String QUERY_STRING_DEFAULT_GRAPH = "?default";
  public static final String X_EVENT_SOURCE_VERSION = "X-EventSource-Version";

  public static final String EVENT_SOURCE_TITLE_HEADER = "X-EventSource-Title";
  public static final String EVENT_SOURCE_DESCRIPTION_HEADER = "X-EventSource-Description";

  public static final String EVENT_SOURCE_CREATOR_HEADER = "X-EventSource-Creator";
  public static final String X_ACCEPT_EVENT_SOURCE_VERSION = "X-Accept-EventSource-Version";
  public static final String COMMIT_TITLE_PARAM = "commitTitle";

  public static final String COMMIT_DESCRIPTION_PARAM = "commitDescription";
  public static final String ACCEPT_HEADER = "Accept";
  public static final String ACCEPT_TURTLE_HEADER = "Accept=" + WebContent.contentTypeTurtle;
  public static final String ACCEPT_JSON_HEADER = "Accept=" + APPLICATION_JSON_UTF8_VALUE;

  public static final String VERSION_UUID = "versionUuid";
  private static final String TRIPLESTORE_BASE_URI = loadSystemEnv("TRIPLESTORE_BASE_URI");
  private static final String EVENT_SOURCE_URI_PREFIX = loadSystemEnv("EVENT_SOURCE_URI_PREFIX");
  private static final String TRIPLESTORE_DATA_URI = TRIPLESTORE_BASE_URI + "/current";
  private static final String TRIPLESTORE_VERSION_URI = EVENT_SOURCE_URI_PREFIX + "/versions/";

  private static final String PATAVI_URI = loadSystemEnv("PATAVI_URI");
  public static final String PATAVI_RESULTS_PATH = "/results";
  public static final String X_AUTH_APPLICATION_KEY = "X-Auth-Application-Key";

  public static String loadSystemEnv(String varName) {
    String envValue = System.getenv(varName);
    if (envValue == null || envValue.isEmpty()) {
      LoggerFactory
              .getLogger(WebConstants.class)
              .error("Cannot start server, no " + varName + " environment variable found");
      System.exit(-1);
    }
    return envValue;
  }

  public String getPataviUri() {
    return PATAVI_URI;
  }

  public URI getPataviMcdaUri() {
    try {
      URIBuilder builder = new URIBuilder(PATAVI_URI);
      builder.setPath("/task");
      builder.addParameter("service", "smaa_v2");
      builder.addParameter("ttl", "PT5M");
      return builder.build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("could not create mcda patavi uri");
    }
  }

  public URI getPataviGemtcUri() {
    try {
      URIBuilder builder = new URIBuilder(PATAVI_URI);
      builder.setPath("/task");
      builder.addParameter("service", "gemtc");
      return builder.build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("could not create gemtc patavi uri");
    }
  }

  public String getTriplestoreBaseUri() {
    return TRIPLESTORE_BASE_URI;
  }

  private static String getEventSourceUriPrefix() {
    return EVENT_SOURCE_URI_PREFIX;
  }

  public String getTriplestoreDataUri() {
    return TRIPLESTORE_DATA_URI;
  }

  public static String getVersionBaseUri() { return TRIPLESTORE_VERSION_URI; }

  public static MediaType getApplicationJsonUtf8() {
    return APPLICATION_JSON_UTF8;
  }

  public static String getApplicationJsonUtf8Value() {
    return APPLICATION_JSON_UTF8_VALUE;
  }

  public static URI buildVersionUri(String versionUuid) {
    if(StringUtils.isEmpty(versionUuid)) return null;
    return UriComponentsBuilder.fromHttpUrl(getEventSourceUriPrefix())
            .path(VERSION_PATH)
            .path(versionUuid)
            .build()
            .toUri();
  }

  public URI buildDatasetUri(String datasetUuid) {
    return UriComponentsBuilder.fromHttpUrl(getTriplestoreBaseUri())
            .path(DATASET_PATH)
            .path(datasetUuid)
            .build()
            .toUri();
  }
}

