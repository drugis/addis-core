'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.results', ['trialverse.study'])
    //services
    .factory('ResultsTableService', require('results/resultsTableService'))
    .factory('ResultsService', require('results/resultsService'))

    //directives
    .directive('resultsTable', require('results/resultsTableDirective'))
    .directive('resultsTableList', require('results/resultsTableListDirective'))
    .directive('resultInputDirective', require('results/resultInputDirective'))
    ;
});
