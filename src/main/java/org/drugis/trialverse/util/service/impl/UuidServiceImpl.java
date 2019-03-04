package org.drugis.trialverse.util.service.impl;

import org.drugis.trialverse.util.service.UuidService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UuidServiceImpl implements UuidService {
  @Override
  public String generate() {
    return UUID.randomUUID().toString();
  }
}
