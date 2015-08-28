'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'datasets', 'datasetUuid', 'graphUuid', 'versionUuid', 'CopyStudyResource', 'UUIDService'];
    var CopyStudyController = function($scope, $modalInstance, datasets, datasetUuid, graphUuid, versionUuid, CopyStudyResource, UUIDService) {

      $scope.datasets = datasets;

      $scope.copyStudy = function(targetDataset) {
        var copyMessage = {
          targetDatasetUuid: targetDataset.datasetUri.split('/')[targetDataset.datasetUri.split('/').length - 1],
          targetGraph: UUIDService.generateGraphUri(),
          sourceGraph: UUIDService.buildGraphUri(graphUuid),
          sourceDatasetUri: UUIDService.buildDatasetUri(datasetUuid),
          sourceVersionUuid: versionUuid
        };
        CopyStudyResource.save(copyMessage).$promise.then(function() {
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
