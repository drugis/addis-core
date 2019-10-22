'use strict';
define(['angular'],
  function(angular) {
    var dependencies = [
      '$scope',
      '$modalInstance',
      'DatasetResource',
      'dataset',
      'userUid',
      'callback'
    ];
    var EditDatasetController = function(
      $scope,
      $modalInstance,
      DatasetResource,
      dataset,
      userUid,
      callback
    ) {
      // functions
      $scope.editDataset = editDataset;
      $scope.cancel = cancel;

      // init
      $scope.dataset = angular.copy(dataset);
      $scope.dataset.description = $scope.dataset.comment;

      function editDataset() {
        $scope.isEditing = true;
        var datasetCommand = {
          title: $scope.dataset.title,
          description: $scope.dataset.description
        };
        DatasetResource.save({
          userUid: userUid,
          datasetUuid: $scope.dataset.datasetUuid,
        }, datasetCommand, function() {
          callback($scope.dataset.title, $scope.dataset.description);
        });
        $modalInstance.close();
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditDatasetController);
  });
