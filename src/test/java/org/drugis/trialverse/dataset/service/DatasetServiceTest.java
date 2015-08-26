package org.drugis.trialverse.dataset.service;

import org.drugis.trialverse.dataset.service.impl.DatasetServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * Created by daan on 26-8-15.
 */
public class DatasetServiceTest {

  DatasetService datasetService;

  @Before
  public void setUp() {
    datasetService = new DatasetServiceImpl();
  }
  
  @Test
  public void testCopy() throws Exception {
    URI targetDatasetUri = new URI("target dataset");
    URI targetGraphUri = new URI("target graph uri");
    URI sourceDatasetUri = new URI("source dataset uri");
    URI sourceGraphUri = new URI("source graph uri");
    URI sourceVersionUri = new URI("source version uri");

    URI copy = datasetService.copy(targetDatasetUri, targetGraphUri, sourceDatasetUri, sourceGraphUri, sourceVersionUri);

  }
}