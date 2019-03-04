package org.drugis.addis.problems;

import org.drugis.addis.problems.service.HostURLCache;
import org.drugis.addis.problems.service.impl.HostURLCacheImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.net.MalformedURLException;

import static org.junit.Assert.*;

public class HostURLCacheTest {

  @InjectMocks
  private HostURLCache hostURLCache;

  @Before
  public void setUp() {
    hostURLCache = new HostURLCacheImpl();
  }

  @Test
  public void testSetHostFromRequestUrl() throws MalformedURLException {
    hostURLCache.setHostFromRequestUrl("https://addis.test.drugis.org/#/problems/3/analyses/4/models/5");
  }

  @Test(expected = MalformedURLException.class)
  public void testGarbageInThrowsException() throws MalformedURLException {
    hostURLCache.setHostFromRequestUrl("jemoederbla");
  }

  @Test
  public void testGetBeforeSetIsEmpty() {
    assertEquals("", hostURLCache.get());
  }

  @Test
  public void testGetAfterSetSucceeds() throws MalformedURLException {
    String hostName = "https://addis.test.drugis.org";
    hostURLCache.setHostFromRequestUrl(hostName + "/#/problems/3/analyses/4/models/5");
    assertEquals(hostName, hostURLCache.get());
  }

}