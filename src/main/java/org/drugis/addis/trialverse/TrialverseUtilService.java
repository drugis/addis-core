package org.drugis.addis.trialverse;

/**
 * Created by connor on 8-4-16.
 */
public class TrialverseUtilService {
  public static String subStringAfterLastSymbol(String inStr, char symbol) {
    return inStr.substring(inStr.lastIndexOf(symbol) + 1);
  }
}
