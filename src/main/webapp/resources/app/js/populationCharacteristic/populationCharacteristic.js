'use strict';
var requires = [
  'populationCharacteristic/populationCharacteristicService'
];
define(requires.concat(['angular']), function(
  PopulationCharacteristicService,
  angular
) {
  return angular.module('trialverse.populationCharacteristic', [
      'ngResource',
      'trialverse.util',
      'trialverse.outcome'
    ])

    //services
    .factory('PopulationCharacteristicService', PopulationCharacteristicService);
});