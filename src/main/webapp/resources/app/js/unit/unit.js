'use strict';
var requires = [
  'unit/repairUnitController',
  'unit/unitService'
];
define(['angular'].concat(requires), function(
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
});