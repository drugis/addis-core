'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'HistoryResource', 'HistoryService', 'DatasetResource', 'SingleDatasetService'];
    var DatasetHistoryController = function($scope, $stateParams, HistoryResource, HistoryService, DatasetResource, SingleDatasetService) {

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

      $scope.historyItems.$promise.then(function(historyResult) {
        $scope.historyItems = HistoryService.addOrderIndex($scope.historyItems);
        var headItemId = ($scope.historyItems[0])['@id'];
        $scope.headVersion = headItemId.substr(headItemId.lastIndexOf('/') + 1);
      });
    };
    return dependencies.concat(DatasetHistoryController);
  });
