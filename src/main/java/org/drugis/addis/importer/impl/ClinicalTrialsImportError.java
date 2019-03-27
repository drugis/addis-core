package org.drugis.addis.importer.impl;

import org.drugis.addis.base.AbstractAddisException;

/**
 * Created by connor on 12-5-16.
 */
public class ClinicalTrialsImportError extends AbstractAddisException {
  public ClinicalTrialsImportError(String s) {
    super(s);
  }
}
