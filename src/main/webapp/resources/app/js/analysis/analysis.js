'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource'];

  return angular.module('addis.analysis',
    dependencies)
    // controllers
    .controller('AddAnalysisController', require('analysis/addAnalysisController'))
    .controller('MetaBenefitRiskStep1Controller', require('analysis/metaBenefitRiskStep1Controller'))

    //services
    .factory('MetaBenefitRiskService', require('analysis/metaBenefitRiskService'))
    //filter

    ;
});
