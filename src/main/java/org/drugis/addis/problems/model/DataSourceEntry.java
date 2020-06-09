package org.drugis.addis.problems.model;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public class DataSourceEntry {
  private final List<Double> scale;
  private final PartialValueFunction pvf;
  private final String source;
  private final URI sourceLink;
  private String id;

  public DataSourceEntry(String id, List<Double> scale, String source, URI sourceLink) {
    this.id = id;
    this.scale = scale;
    this.pvf = null;
    this.source = source;
    this.sourceLink = sourceLink;
  }

  public DataSourceEntry(String id, String source, URI sourceLink) {
    this(id, null, source, sourceLink);
  }

  public String getSource() {
    return source;
  }

  public URI getSourceLink() {
    return sourceLink;
  }

  public List<Double> getScale() {
    return scale;
  }

  public PartialValueFunction getPvf() {
    return pvf;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataSourceEntry that = (DataSourceEntry) o;
    return Objects.equals(scale, that.scale) &&
        Objects.equals(pvf, that.pvf) &&
        Objects.equals(source, that.source) &&
        Objects.equals(sourceLink, that.sourceLink) &&
        Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {

    return Objects.hash(scale, pvf, source, sourceLink, id);
  }
}
