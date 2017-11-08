'use strict';
var requires = [
  'intervention/addInterventionController',
  'intervention/editInterventionController',
  'intervention/interventionService',
  'intervention/dosageService',
  'intervention/constraintDirective',
  'intervention/boundDirective',
  'intervention/scaledUnitInputDirective'
];
define(requires.concat(['angular', 'angular-resource']), function(
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
});