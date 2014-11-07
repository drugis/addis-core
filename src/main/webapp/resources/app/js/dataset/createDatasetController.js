'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'DatasetResource', 'successCallback'];
    var CreateDatasetController = function($scope, $state, $modalInstance, DatasetResource, successCallback) {
      $scope.dataset = {};
      $scope.createDataset = function() {
        DatasetResource.save($scope.dataset).$promise.then(function(dataset) {
          console.log('dataset ' + dataset + 'created');
          successCallback(dataset);
        });
        $modalInstance.close();
      };
      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(CreateDatasetController);
  });
