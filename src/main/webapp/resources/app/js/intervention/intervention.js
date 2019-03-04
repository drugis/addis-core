'use strict';
define([
  './addInterventionController',
  './editInterventionController',
  './interventionService',
  './dosageService',
  './constraintDirective',
  './boundDirective',
  './scaledUnitInputDirective',
  'angular', 
  'angular-resource'
],
  function(
    AddInterventionController,
    EditInterventionController,
    InterventionService,
    DosageService,
    constraint,
    bound,
    scaledUnitInput,
    angular
  ) {
    var dependencies = ['ngResource', 'trialverse.util'];
    return angular.module('addis.interventions', dependencies)
      // controllers
      .controller('AddInterventionController', AddInterventionController)
      .controller('EditInterventionController', EditInterventionController)

      //services
      .factory('InterventionService', InterventionService)
      .factory('DosageService', DosageService)

      //directives
      .directive('constraint', constraint)
      .directive('bound', bound)
      .directive('scaledUnitInput', scaledUnitInput);
  }
);
