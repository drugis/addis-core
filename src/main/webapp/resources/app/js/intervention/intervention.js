'use strict';

define(function(require) {
  var angular = require('angular');
  var dependencies = ['ngResource', 'trialverse.util'];

  return angular.module('addis.interventions', dependencies)
    // controllers
    .controller('AddInterventionController', require('intervention/addInterventionController'))
    .controller('EditInterventionController', require('intervention/editInterventionController'))

  //services
    .factory('InterventionService', require('intervention/interventionService'))
    .factory('DosageService', require('intervention/dosageService'))

  //directives
  .directive('constraint', require('intervention/constraintDirective'))
  .directive('bound', require('intervention/boundDirective'))
  .directive('scaledUnitInput', require('intervention/scaledUnitInputDirective'))
  ;
});
