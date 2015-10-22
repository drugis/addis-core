package org.drugis.addis.exception;

public class ResourceDoesNotExistException extends Exception {
  private static final long serialVersionUID = 9073600696022098494L;

  public ResourceDoesNotExistException() {
    super();
  }

  public ResourceDoesNotExistException(Throwable t) {
    super(t);
  }

  public ResourceDoesNotExistException(String s) {
    super(s);
  }
}