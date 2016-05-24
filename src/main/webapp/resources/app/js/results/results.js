'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.results', ['trialverse.study'])
    //services
    .factory('ResultsTableService', require('results/resultsTableService'))
    .factory('ResultsService', require('results/resultsService'))
    .factory('NonConformantMeasurementTableService', require('results/nonConformantMeasurementTableService'))

    //directives
    .directive('resultsTable', require('results/resultsTableDirective'))
    .directive('resultsTableList', require('results/resultsTableListDirective'))
    .directive('resultInputDirective', require('results/resultInputDirective'))
    .directive('nonConformantMeasurementTable', require('results/nonConformantMeasurementTableDirective'))
    ;
});
