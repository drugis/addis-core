package org.drugis.addis.patavitask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

/**
 * Created by connor on 26-6-14.
 */
public class PataviTask {

  private String status;

  private URI self;

  private URI results;

  private URI updates;

  public PataviTask() {
  }

  public PataviTask(String pataviResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(pataviResponse);
    this.status = jsonNode.get("status").asText();
    this.self = URI.create(jsonNode.get("_links").get("self").get("href").asText());
    this.updates = URI.create(jsonNode.get("_links").get("updates").get("href").asText());
    if (jsonNode.get("_links").get("results") != null) {
      this.results = URI.create(jsonNode.get("_links").get("results").get("href").asText());
    }
  }

  public String getStatus() {
    return status;
  }

  public URI getSelf() {
    return self;
  }

  public URI getResults() {
    return results;
  }

  public URI getUpdates() {
    return updates;
  }

  public Boolean hasResults() {
    return this.results != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PataviTask that = (PataviTask) o;
    return Objects.equals(status, that.status) && Objects.equals(self, that.self) && Objects.equals(results, that.results) && Objects.equals(updates, that.updates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, self, results, updates);
  }
}
