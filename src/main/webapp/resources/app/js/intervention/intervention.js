'use strict';

define(function (require) {
  var angular = require('angular');
  var dependencies = ['ngResource', 'trialverse.util'];

  return angular.module('addis.interventions',
    dependencies)
    // controllers
    .controller('AddInterventionController', require('intervention/addInterventionController'))

    //services
    .factory('UnitNamesService', require('intervention/unitNamesService'))

    //directives
    .directive('constraint', require('intervention/constraintDirective'))

    ;
});
