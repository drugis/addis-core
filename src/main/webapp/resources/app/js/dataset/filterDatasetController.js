'use strict';
define([], function() {
  var dependencies = ['$scope', '$modalInstance', 'interventions', 'variables', 'filterSelections', 'callback'];
  var FilterDatasetController = function($scope, $modalInstance, interventions, variables, filterSelections, callback) {
    // functions
    $scope.cancel = cancel;
    $scope.updateFilteredStudies = updateFilteredStudies;

    // init
    $scope.interventions = interventions;
    $scope.variables = variables;
    $scope.filterSelections = filterSelections;

    function updateFilteredStudies() {
      callback($scope.filterSelections);
      $modalInstance.close();
    }

    function cancel() {
      $modalInstance.close();
    }
  };
  return dependencies.concat(FilterDatasetController);
});