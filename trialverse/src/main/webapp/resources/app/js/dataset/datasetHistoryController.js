'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'HistoryResource', 'DatasetResource', 'SingleDatasetService'];
    var DatasetHistoryController = function($scope, $stateParams, HistoryResource, DatasetResource, SingleDatasetService) {

      $scope.datasetUUID = $stateParams.datasetUUID;
      $scope.userUid = $stateParams.userUid;

      DatasetResource.get($stateParams, function(response) {
        SingleDatasetService.reset();
        SingleDatasetService.loadStore(response.data).then(function() {
          SingleDatasetService.queryDataset().then(function(queryResult) {
            $scope.dataset = queryResult[0];
            $scope.dataset.uuid = $stateParams.datasetUUID;
          });
        });
      });

      $scope.historyItems = HistoryResource.query($stateParams);

      function oldToNew(a, b) {
        return b.i - a.i;
      }

      $scope.historyItems.$promise.then(function(historyResult) {
        $scope.historyItems.sort(oldToNew);
        var headItemUri = ($scope.historyItems[0].uri);
        $scope.headVersion = headItemUri.substr(headItemUri.lastIndexOf('/') + 1);
      });
    };
    return dependencies.concat(DatasetHistoryController);
  });
