package org.drugis.addis.util;

/**
 * Created by daan on 28-3-14.
 */
public class JSONUtils {

  /*
   * remove all non-alphanumeric characters except spaces; replace spaces with dashes.
   */
  public static String  createKey(String value) {
    return value.replaceAll("[^a-zA-Z0-9 ]","").replace(" ", "-").toLowerCase();
  }
}
