'use strict';

define([
  './addOutcomeController',
  './editOutcomeController',
  '../study/repair/repairService',
  './outcomeService',
  'angular',
  'angular-resource'
], function(
  AddOutcomeController,
  EditOutcomeController,
  RepairService,
  OutcomeService,
  angular
) {
    var dependencies = [
      'ngResource',
      'trialverse.measurementMoment',
      'trialverse.study',
      'trialverse.util'
    ];
    return angular.module('trialverse.outcome',
      dependencies)
      // controllers
      .controller('AddOutcomeController', AddOutcomeController)
      .controller('EditOutcomeController', EditOutcomeController)

      //services
      .factory('RepairService', RepairService)
      .factory('OutcomeService', OutcomeService)

      ;
  }
);
