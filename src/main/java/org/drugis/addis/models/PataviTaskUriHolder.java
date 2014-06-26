package org.drugis.addis.models;

/**
 * Created by connor on 26-6-14.
 */
public class PataviTaskUriHolder {
  private String uri;

  public PataviTaskUriHolder() {
  }

  public PataviTaskUriHolder(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PataviTaskUriHolder)) return false;

    PataviTaskUriHolder that = (PataviTaskUriHolder) o;

    if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return uri != null ? uri.hashCode() : 0;
  }
}
