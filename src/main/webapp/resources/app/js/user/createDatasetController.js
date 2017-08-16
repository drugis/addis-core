'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modalInstance', 'DatasetResource', 'callback'];
    var CreateDatasetController = function($scope, $stateParams, $modalInstance, DatasetResource, callback) {
      // functions
      $scope.createDataset = createDataset;
      $scope.cancel = cancel;

      // init
      $scope.dataset = {};

      function createDataset() {
        DatasetResource.save($stateParams, $scope.dataset).$promise.then(function() {
          callback();
        });
        $modalInstance.close();
      }

      function cancel() {
        $modalInstance.dismiss('cancel');
      }
    };
    return dependencies.concat(CreateDatasetController);
  });