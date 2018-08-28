'use strict';
define([
  './resultsTableService',
  './resultsService',
  './nonConformantMeasurementTableService',
  './splitOutcomeController',
  './resultsTableDirective',
  './resultsTableListDirective',
  './resultInputDirective',
  './nonConformantMeasurementTableDirective',
  'angular'
], function(
  ResultsTableService,
  ResultsService,
  NonConformantMeasurementTableService,
  SplitOutcomeController,
  resultsTable,
  resultsTableList,
  resultInputDirective,
  nonConformantMeasurementTable,
  angular
) {
  return angular.module('trialverse.results', ['trialverse.study'])
    //services
    .factory('ResultsTableService', ResultsTableService)
    .factory('ResultsService', ResultsService)
    .factory('NonConformantMeasurementTableService', NonConformantMeasurementTableService)

    // controllers
    .controller('SplitOutcomeController', SplitOutcomeController)

    //directives
    .directive('resultsTable', resultsTable)
    .directive('resultsTableList', resultsTableList)
    .directive('resultInputDirective', resultInputDirective)
    .directive('nonConformantMeasurementTable', nonConformantMeasurementTable);
}
);
