'use strict';
define([
  './addAnalysisController',
  './benefitRiskStep1Controller',
  './benefitRiskStep2Controller',
  './benefitRiskController',
  './abstractBenefitRiskController',
  './setStudyBaselineController',
  './benefitRiskService',
  './benefitRiskErrorService',
  './singleStudyBenefitRiskService',
  './benefitRiskStep1Service',
  './benefitRiskStep2Service',
  './directives/studySelectDirective',
  'angular',
  'angular-resource',
  'mcda-web/js/workspace/workspace',
  '../project/project',
  'gemtc-web/js/services',
  'gemtc-web/js/resources',
  '../resources'
], function(
  AddAnalysisController,
  BenefitRiskStep1Controller,
  BenefitRiskStep2Controller,
  BenefitRiskController,
  AbstractBenefitRiskController,
  SetStudyBaselineController,
  BenefitRiskService,
  BenefitRiskErrorService,
  SingleStudyBenefitRiskService,
  BenefitRiskStep1Service,
  BenefitRiskStep2Service,
  studySelect,
  angular
) {
    var dependencies = [
      'ngResource',
      'elicit.workspace',
      'gemtc.services',
      'addis.project',
      'gemtc.resources',
      'addis.resources'
    ];
    return angular.module('addis.analysis', dependencies)
      // controllers
      .controller('AddAnalysisController', AddAnalysisController)
      .controller('BenefitRiskStep1Controller', BenefitRiskStep1Controller)
      .controller('BenefitRiskStep2Controller', BenefitRiskStep2Controller)
      .controller('BenefitRiskController', BenefitRiskController)
      .controller('AbstractBenefitRiskController', AbstractBenefitRiskController)
      .controller('SetStudyBaselineController', SetStudyBaselineController)

      //services
      .factory('BenefitRiskService', BenefitRiskService)
      .factory('BenefitRiskErrorService', BenefitRiskErrorService)
      .factory('SingleStudyBenefitRiskService', SingleStudyBenefitRiskService)
      .factory('BenefitRiskStep1Service', BenefitRiskStep1Service)
      .factory('BenefitRiskStep2Service', BenefitRiskStep2Service)

      //directives
      .directive('studySelect', studySelect)

      ;
  }
);
