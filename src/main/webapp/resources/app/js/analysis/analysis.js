'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource'];

  return angular.module('addis.analysis',
    dependencies)
    // controllers
    .controller('AddAnalysisController', require('analysis/addAnalysisController'))
    .controller('BenefitRiskStep1Controller', require('analysis/benefitRiskStep1Controller'))
    .controller('BenefitRiskStep2Controller', require('analysis/benefitRiskStep2Controller'))
    .controller('BenefitRiskController', require('analysis/benefitRiskController'))
    .controller('AbstractBenefitRiskController', require('analysis/abstractBenefitRiskController'))
    //services
    .factory('BenefitRiskService', require('analysis/benefitRiskService'))
    //directives
    .directive('studySelect', require('analysis/directives/studySelectDirective'))

    ;
});
