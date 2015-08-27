'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'datasets', 'CopyStudyResource'];
    var CopyStudyController = function($scope, $modalInstance, datasets, CopyStudyResource) {

      $scope.datasets = datasets;

      $scope.copyStudy = function () {
        CopyStudyResource.save($scope.targetDataset).then(function() {
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
