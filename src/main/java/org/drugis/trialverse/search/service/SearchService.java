package org.drugis.trialverse.search.service;


import org.drugis.trialverse.search.model.SearchResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by connor on 01/09/15.
 */
public interface SearchService {

  public List<SearchResult> searchStudy(String searchTerm) throws IOException, URISyntaxException;
}
