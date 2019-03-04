'use strict';
define([
  './networkMetaAnalysisService',
  './contrastEvidenceTableDirective',
  './absoluteEvidenceTableDirective'
], function(
  NetworkMetaAnalysisService,
  contrastEvidenceTable,
  absoluteEvidenceTable
) {
    return angular.module('addis.networkMetaAnalysis', [])
      .factory('NetworkMetaAnalysisService', NetworkMetaAnalysisService)
      .directive('absoluteEvidenceTable', absoluteEvidenceTable)
      .directive('contrastEvidenceTable', contrastEvidenceTable)
      ;
  }
);
