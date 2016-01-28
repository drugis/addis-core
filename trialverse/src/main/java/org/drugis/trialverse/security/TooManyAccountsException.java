package org.drugis.trialverse.security;

/**
 * Created by connor on 9/10/15.
 */
public class TooManyAccountsException extends Exception {

  public TooManyAccountsException() {
    super("Expected zero or one accounts but found more ");
  }
}
