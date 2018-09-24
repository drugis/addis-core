package org.drugis.addis.trialverse.service;

import org.drugis.addis.trialverse.service.impl.MappingServiceImpl;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MappingServiceTest {

  @Mock
  private WebConstants webConstants;

  @InjectMocks
  private MappingService mappingService = new MappingServiceImpl();

  @Before
  public void setUp() {
    initMocks(this);
    when(webConstants.getTriplestoreBaseUri()).thenReturn("http://something.com");
  }

  @Test
  public void testGetJenaUrl() {
    VersionMapping mapping = new VersionMapping("http://localhost:8080/datasets/uuid1", "daan@foo.bar.com", "https://trials.druigis.org/datasets/permauuid");
    String jenaURL = mappingService.getJenaURL(mapping);
    assertEquals("http://something.com/datasets/uuid1", jenaURL);
  }


}