'use strict';
define(['./populationCharacteristicService', 'angular'],
  function(
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
  }
);
