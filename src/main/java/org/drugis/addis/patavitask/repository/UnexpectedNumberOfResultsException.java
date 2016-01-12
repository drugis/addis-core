package org.drugis.addis.patavitask.repository;

/**
 * Created by connor on 11/18/15.
 */
public class UnexpectedNumberOfResultsException extends Exception {
  public UnexpectedNumberOfResultsException(String message) {
    super(message);
  }
}
