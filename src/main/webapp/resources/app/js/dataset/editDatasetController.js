'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$stateParams', '$modalInstance', 'DatasetResource', 'dataset', 'userUid', 'callback'];
    var EditDatasetController = function($scope, $stateParams, $modalInstance, DatasetResource, dataset, userUid, callback) {
      $scope.dataset = angular.copy(dataset);
      $scope.dataset.description = $scope.dataset.comment;
      $scope.editDataset = function() {
        $scope.isEditing = true;
        var datasetCommand = {
          title: $scope.dataset.title,
          description: $scope.dataset.description
        };
        DatasetResource.save({
          userUid: userUid,
          datasetUuid: $scope.dataset.datasetUri,
        }, datasetCommand, function() {
          callback($scope.dataset.title, $scope.dataset.description);
        });
        $modalInstance.close();
      };
      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditDatasetController);
  });
