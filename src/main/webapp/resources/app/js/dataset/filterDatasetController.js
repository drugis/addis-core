'use strict';
define([], function() {
  var dependencies = ['$scope', '$modalInstance', 'interventions', 'variables', 'filterSelections', 'callback'];
  var FilterDatasetController = function($scope, $modalInstance, interventions, variables, filterSelections, callback) {

    $scope.interventions = interventions;
    $scope.variables = variables;
    $scope.filterSelections = filterSelections;

    $scope.updateFilteredStudies = function() {
      callback($scope.filterSelections);
      $modalInstance.dismiss('cancel');
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(FilterDatasetController);
});
