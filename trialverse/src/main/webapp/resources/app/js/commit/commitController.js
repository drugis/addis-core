'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$injector', '$stateParams', '$modalInstance', 'userUid', 'datasetUuid', 'graphUuid', 'callback', 'itemServiceName'];
    var CommitController = function($scope, $injector, $stateParams, $modalInstance, userUid, datasetUuid, graphUuid, callback, itemServiceName) {

      $scope.userUid = userUid;
      $scope.datasetUuid = datasetUuid;
      $scope.graphUuid = graphUuid;
      $scope.itemServiceName = itemServiceName;

      $scope.changesCommited = function(newVersion) {
        callback(newVersion);
        $modalInstance.close();
      };

      $scope.commitCancelled = function() {
        $modalInstance.dismiss();
      };

    };

    return dependencies.concat(CommitController);

  });