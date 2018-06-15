package org.drugis.addis.problems.service.impl;

import org.drugis.addis.problems.service.HostURLCache;

import java.net.MalformedURLException;
import java.net.URL;

public class HostURLCacheImpl implements HostURLCache {
  private String host = "";

  @Override
  public void setHostFromRequestUrl(String path) throws MalformedURLException {
    URL uri = new URL(path);
    host = uri.getProtocol() + "://" + uri.getHost();
  }

  @Override
  public String get() {
    return host;
  }
}
