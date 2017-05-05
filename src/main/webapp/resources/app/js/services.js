'use strict';
define(function(require) {
  var angular = require('angular');
  return angular.module('addis.services', ['gemtc.services'])
    .factory('SingleStudyBenefitRiskService', require('services/singleStudyBenefitRiskService'))
    .factory('NetworkMetaAnalysisService', require('services/networkMetaAnalysisService'))
    .factory('NetworkPlotService', require('gemtc-web/analyses/networkPlotService'))
    ;
});