'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$injector', '$stateParams', '$modalInstance', 'datasetUuid', 'graphUuid', 'callback', 'itemServiceName'];
    var CommitController = function($scope, $injector, $stateParams, $modalInstance, datasetUuid, graphUuid, callback, itemServiceName) {

      $scope.datasetUuid = datasetUuid;
      $scope.graphUuid = graphUuid;
      $scope.itemServiceName = itemServiceName;

      $scope.changesCommited = function() {
        callback();
        $modalInstance.close();
      };

      $scope.commitCancelled = function() {
        $modalInstance.dismiss();
      };

    };

    return dependencies.concat(CommitController);

  });