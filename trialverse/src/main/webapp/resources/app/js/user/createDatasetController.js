'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modalInstance', 'DatasetResource', 'callback'];
    var CreateDatasetController = function($scope, $stateParams, $modalInstance, DatasetResource, callback) {
      $scope.dataset = {};
      $scope.createDataset = function() {
        DatasetResource.save($stateParams, $scope.dataset).$promise.then(function() {
          callback();
        });
        $modalInstance.close();
      };
      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(CreateDatasetController);
  });
