'use strict';
define(
  [
    'angular',
    './services/singleStudyBenefitRiskService',
    './services/networkMetaAnalysisService',
    'gemtc-web/js/services'
  ],
  function(
    angular,
    SingleStudyBenefitRiskService,
    NetworkMetaAnalysisService
  ) {
    return angular.module('addis.services', ['gemtc.services'])
      .factory('SingleStudyBenefitRiskService', SingleStudyBenefitRiskService)
      .factory('NetworkMetaAnalysisService', NetworkMetaAnalysisService)
  }
);
