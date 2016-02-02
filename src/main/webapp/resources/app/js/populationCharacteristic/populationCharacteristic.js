'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('trialverse.populationCharacteristic', [
    'ngResource',
    'trialverse.util',
    'trialverse.outcome'
  ])

  // controllers
  .controller('CreatePopulationCharacteristicController', require('populationCharacteristic/createPopulationCharacteristicController'))
  .controller('EditPopulationCharacteristicController', require('populationCharacteristic/editPopulationCharacteristicController'))

  //services
  .factory('PopulationCharacteristicService', require('populationCharacteristic/populationCharacteristicService'));
});