'use strict';

define(function (require) {
  var angular = require('angular');

  return angular.module('trialverse.results', [])
    //services
    .factory('ResultsTableService', require('results/resultsTableService'))

    //directives
    .directive('resultsTable', require('results/resultsTableDirective'))
    ;
});
