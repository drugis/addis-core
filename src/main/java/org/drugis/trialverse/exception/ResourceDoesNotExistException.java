package org.drugis.trialverse.exception;

/**
 * Created by connor on 27-11-14.
 */
public class ResourceDoesNotExistException extends Exception {
  private static final long serialVersionUID = 9073600696022098494L;

  public ResourceDoesNotExistException() {
    super();
  }

  public ResourceDoesNotExistException(Throwable t) {
    super(t);
  }
}
