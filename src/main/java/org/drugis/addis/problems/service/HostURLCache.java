package org.drugis.addis.problems.service;

import java.net.MalformedURLException;

public interface HostURLCache {
  void setHostFromRequestUrl(String s) throws MalformedURLException;

  String get();
}
