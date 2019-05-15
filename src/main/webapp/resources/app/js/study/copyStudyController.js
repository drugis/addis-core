'use strict';
define([],
  function() {
    var dependencies = [
      '$scope',
      '$modalInstance',
      'datasets',
      'loggedInUserId',
      'datasetUuid',
      'graphUuid',
      'versionUuid',
      'CopyStudyResource',
      'UUIDService'
    ];
    var CopyStudyController = function(
      $scope,
      $modalInstance,
      datasets,
      loggedInUserId,
      datasetUuid,
      graphUuid,
      versionUuid,
      CopyStudyResource,
      UUIDService
    ) {
      // functions
      $scope.copyStudy = copyStudy;
      $scope.cancel = cancel;

      // init
      $scope.datasets = datasets;

      function copyStudy(targetDataset) {
        $scope.isCopying = true;
        $scope.targetDatasetUuid = targetDataset.uri.split('/')[targetDataset.uri.split('/').length - 1];
        $scope.targetGraphUuid = UUIDService.generate();
        $scope.loggedInUserId = loggedInUserId;

        var copyMessage = {
          targetDatasetUuid: $scope.targetDatasetUuid,
          targetGraphUuid: $scope.targetGraphUuid,
          copyOf: UUIDService.buildGraphUri(datasetUuid, versionUuid, graphUuid)
        };
        CopyStudyResource.copy(copyMessage, function(__, responseHeaders) {
          var newVersion = responseHeaders('X-EventSource-Version');
          newVersion = newVersion.split('/')[4];
          $scope.newVersionUuid = newVersion;
          $scope.copyComplete = true;
        },
          function() {
            $modalInstance.close();
          });
      }

      function cancel() {
        $modalInstance.close();
      }

    };
    return dependencies.concat(CopyStudyController);
  });
