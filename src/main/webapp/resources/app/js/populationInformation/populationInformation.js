'use strict';

define(function(require) {
  var angular = require('angular');

  return angular.module('trialverse.populationInformation', [
    'ngResource',
    'trialverse.util',
    'trialverse.study'
  ])

  // controllers
  .controller('EditPopulationInformationController', require('populationInformation/editPopulationInformationController'))

  //services
  .factory('PopulationInformationService', require('populationInformation/populationInformationService'));
});