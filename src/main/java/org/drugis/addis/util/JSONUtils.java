package org.drugis.addis.util;

import org.springframework.stereotype.Component;

/**
 * Created by daan on 28-3-14.
 */
@Component
public class JSONUtils {

  /*
   * remove all non-alphanumeric characters except spaces; replace spaces with dashes.
   */
  public String createKey(String value) {
    return value.replaceAll("[^a-zA-Z0-9 ]","").replace(" ", "-").toLowerCase();
  }
}
