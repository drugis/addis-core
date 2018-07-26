'use strict';
var requires = [
  'services/singleStudyBenefitRiskService',
  'services/networkMetaAnalysisService',
  'gemtc-web/analyses/networkPlotService'
];
define(['angular'].concat(requires), function(angular,
  SingleStudyBenefitRiskService,
  NetworkMetaAnalysisService,
  NetworkPlotService
) {
  return angular.module('addis.services', ['gemtc.services'])
    .factory('SingleStudyBenefitRiskService', SingleStudyBenefitRiskService)
    .factory('NetworkMetaAnalysisService', NetworkMetaAnalysisService)
    .factory('NetworkPlotService', NetworkPlotService)
    ;
});
