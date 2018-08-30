'use strict';
define(
  [
    'angular',
    './services/singleStudyBenefitRiskService',
    './services/networkMetaAnalysisService',
    'gemtc-web/js/analyses/networkPlotService'
  ],
  function(
    angular,
    SingleStudyBenefitRiskService,
    NetworkMetaAnalysisService,
    NetworkPlotService
  ) {
    return angular.module('addis.services', ['gemtc.services'])
      .factory('SingleStudyBenefitRiskService', SingleStudyBenefitRiskService)
      .factory('NetworkMetaAnalysisService', NetworkMetaAnalysisService)
      .factory('NetworkPlotService', NetworkPlotService);
  }
);
