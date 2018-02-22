'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modalInstance', 'DatasetResource', 'callback'];
    var CreateDatasetController = function($scope, $stateParams, $modalInstance, DatasetResource, callback) {
      // functions
      $scope.createDataset = createDataset;
      $scope.cancel = cancel;
      $scope.checkDatasetName = checkDatasetName;
      $scope.uploadExcel = uploadExcel;
      $scope.importDataset = importDataset;
      
      // init
      $scope.dataset = {};

      function checkDatasetName(newName){

      }

      function uploadExcel(){

      }

      function importDataset(){

      }

      function createDataset() {
        DatasetResource.save($stateParams, $scope.dataset).$promise.then(function() {
          callback();
        });
        $modalInstance.close();
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(CreateDatasetController);
  });