package org.drugis.trialverse.dataset.model;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by joris on 8-11-16.
 */
public class AdvancedVersionNode extends VersionNode {
  private Set<Pair<URI, URI>> graphRevisions = new HashSet<>();

  public AdvancedVersionNode() {
  }

  public AdvancedVersionNode(String uri, String versionTitle, Date versionDate, String description, String creator, Integer userId, Integer historyOrder, String applicationName) {
    super(uri, versionTitle, versionDate, description, creator, userId, historyOrder, applicationName);
  }

  public Set<Pair<URI, URI>> getGraphRevisions() {
    return ImmutableSet.copyOf(this.graphRevisions);
  }

  public void addGraphRevisionPair(URI graphUri, URI revisionUri) {
    this.graphRevisions.add(Pair.of(graphUri, revisionUri));
  }

  public VersionNode simplify() {
    return new VersionNode(this.getUri(),
            this.getVersionTitle(), this.getVersionDate(),
            this.getDescription(), this.getCreator(),
            this.getUserId(), this.getHistoryOrder(),
            this.getApplicationName());
  }
}
