'use strict';
define(
  [
    'angular',
    './services/singleStudyBenefitRiskService',
    'gemtc-web/js/services'
  ],
  function(
    angular,
    SingleStudyBenefitRiskService
  ) {
    return angular.module('addis.services', ['gemtc.services'])
      .factory('SingleStudyBenefitRiskService', SingleStudyBenefitRiskService);
     
  }
);
