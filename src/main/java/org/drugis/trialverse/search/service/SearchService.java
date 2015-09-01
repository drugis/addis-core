package org.drugis.trialverse.search.service;


import org.drugis.trialverse.search.controller.StudySearchResult;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by connor on 01/09/15.
 */
public interface SearchService {

  public List<StudySearchResult> searchStudy(String searchTerm) throws IOException, URISyntaxException;
}
