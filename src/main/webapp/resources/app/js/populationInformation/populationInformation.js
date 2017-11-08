'use strict';
var requires = [
  'populationInformation/editPopulationInformationController',
  'populationInformation/populationInformationService'
];
define(requires.concat(['angular', 'angular-resource']), function(
  EditPopulationInformationController,
  PopulationInformationService,
  angular
) {

  var dependencies = [
    'ngResource',
    'trialverse.util',
    'trialverse.study'
  ];
  return angular.module('trialverse.populationInformation', dependencies)

    // controllers
    .controller('EditPopulationInformationController', EditPopulationInformationController)

    //services
    .factory('PopulationInformationService', PopulationInformationService)

  ;
});