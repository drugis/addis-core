'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'datasets', 'datasetUuid', 'graphUuid', 'versionUuid', 'CopyStudyResource', 'UUIDService'];
    var CopyStudyController = function($scope, $modalInstance, datasets, datasetUuid, graphUuid, versionUuid, CopyStudyResource, UUIDService) {

      $scope.datasets = datasets;

      $scope.copyStudy = function (targetDataset) {
        var copyToPost = angular.copy(targetDataset);
        copyToPost.targetGraph = UUIDService.generateGraphUri();
        copyToPost.sourceGraph = UUIDService.buildGraphUri(graphUuid);
        copyToPost.sourceDatasetUri = UUIDService.buildDatasetUri(datasetUuid);
        copyToPost.sourceVersion = UUIDService.buildVersionUri(versionUuid);
        CopyStudyResource.save(copyToPost).$promise.then(function() {
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
