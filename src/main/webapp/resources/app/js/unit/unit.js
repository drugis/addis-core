'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('trialverse.unit', [
      'trialverse.study'
    ])
    // controllers
    .controller('RepairUnitController', require('unit/repairUnitController'))

  //services
  .factory('UnitService', require('unit/unitService'));
});
