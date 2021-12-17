package org.drugis.addis.patavitask;

import java.net.URI;
import java.util.Objects;

/**
 * Created by connor on 26-6-14.
 */
public class PataviTaskUriHolder {
  private URI uri;

  public PataviTaskUriHolder() {
  }

  public PataviTaskUriHolder(URI uri) {
    this.uri = uri;
  }

  public URI getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PataviTaskUriHolder that = (PataviTaskUriHolder) o;
    return Objects.equals(uri, that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uri);
  }
}
