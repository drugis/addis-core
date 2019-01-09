'use strict';
define([
  './addAnalysisController',
  './benefitRiskStep1Controller',
  './benefitRiskStep2Controller',
  './benefitRiskController',
  './abstractBenefitRiskController',
  './benefitRiskService',
  './benefitRiskErrorService',
  './directives/studySelectDirective',
  'angular',
  'angular-resource',
  'mcda-web/js/workspace/workspace',
  '../project/project',
  'gemtc-web/js/resources',
  '../resources'
], function(
  AddAnalysisController,
  BenefitRiskStep1Controller,
  BenefitRiskStep2Controller,
  BenefitRiskController,
  AbstractBenefitRiskController,
  BenefitRiskService,
  BenefitRiskErrorService,
  studySelect,
  angular
) {
    var dependencies = ['ngResource', 'elicit.workspace', 'gemtc.resources', 'addis.resources', 'addis.project'];
    return angular.module('addis.analysis',
      dependencies)
      // controllers
      .controller('AddAnalysisController', AddAnalysisController)
      .controller('BenefitRiskStep1Controller', BenefitRiskStep1Controller)
      .controller('BenefitRiskStep2Controller', BenefitRiskStep2Controller)
      .controller('BenefitRiskController', BenefitRiskController)
      .controller('AbstractBenefitRiskController', AbstractBenefitRiskController)
      //services
      .factory('BenefitRiskService', BenefitRiskService)
      .factory('BenefitRiskErrorService', BenefitRiskErrorService)

      //directives
      .directive('studySelect', studySelect)

      ;
  }
);
