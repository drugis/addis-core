'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'datasets', 'datasetUuid', 'graphUuid', 'versionUuid', 'CopyStudyResource', 'UUIDService'];
    var CopyStudyController = function($scope, $modalInstance, datasets, datasetUuid, graphUuid, versionUuid, CopyStudyResource, UUIDService) {

      $scope.datasets = datasets;

      $scope.copyStudy = function(targetDataset) {
        var copyMessage = {
          targetDatasetUuid: targetDataset.uri.split('/')[targetDataset.uri.split('/').length - 1],
          targetGraphUuid: UUIDService.generate(),
          copyOf: UUIDService.buildGraphUri(datasetUuid, versionUuid, graphUuid)
        };
        CopyStudyResource.copy(copyMessage).$promise.then(function() {
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(CopyStudyController);
  });
