'use strict';
define([
  'angular',
  './repairUnitController',
  './unitService'
],
  function(
    angular,
    RepairUnitController,
    UnitService) {
    return angular.module('trialverse.unit', [
      'trialverse.study'
    ])
      // controllers
      .controller('RepairUnitController', RepairUnitController)

      //services
      .factory('UnitService', UnitService);
  }
);
