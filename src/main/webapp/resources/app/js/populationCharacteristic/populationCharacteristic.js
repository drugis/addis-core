'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('trialverse.populationCharacteristic', [
    'ngResource',
    'trialverse.util',
    'trialverse.outcome'
  ])

  //services
  .factory('PopulationCharacteristicService', require('populationCharacteristic/populationCharacteristicService'));
});
