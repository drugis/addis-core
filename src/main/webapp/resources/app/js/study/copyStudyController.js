'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'datasets', 'userUuid', 'datasetUuid', 'graphUuid', 'versionUuid', 'CopyStudyResource', 'UUIDService'];
    var CopyStudyController = function($scope, $modalInstance, datasets, userUuid, datasetUuid, graphUuid, versionUuid, CopyStudyResource, UUIDService) {

      $scope.datasets = datasets;

      $scope.copyStudy = function(targetDataset) {
        $scope.targetDatasetUuid = targetDataset.uri.split('/')[targetDataset.uri.split('/').length - 1];
        $scope.targetGraphUuid = UUIDService.generate();
        $scope.userUuid = userUuid;

        var copyMessage = {
          targetDatasetUuid: $scope.targetDatasetUuid,
          targetGraphUuid: $scope.targetGraphUuid,
          copyOf: UUIDService.buildGraphUri(datasetUuid, versionUuid, graphUuid)
        };
        CopyStudyResource.copy(copyMessage, function(newVersion, responseHeaders) {
            var newVersion = responseHeaders('X-EventSource-Version');
            newVersion = newVersion.split('/')[4];
            $scope.newVersionUuid = newVersion;
            $scope.copyComplete = true;
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
