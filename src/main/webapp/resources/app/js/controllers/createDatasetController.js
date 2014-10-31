'use strict';
define([], function() {
  var dependencies = ['$scope', '$state', 'DatasetService'];
  var CreateDatasetController = function($scope, $state, DatasetService) {
    $scope.dataset = {
      owner: window.config.user.id
    };
    $scope.createDataset = function() {
      DatasetService.createDataset($scope.dataset, function(dataset) {
        $state.go('dataset', {datasetUid: dataset.uid});
      })
    };
  };
  return dependencies.concat(CreateDatasetController);
});
