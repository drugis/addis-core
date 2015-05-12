'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'HistoryResource', 'HistoryService', 'DatasetResource', 'DatasetService'];
    var DatasetHistoryController = function($scope, $stateParams, HistoryResource, HistoryService, DatasetResource, DatasetService) {
     
      $scope.datasetUUID = $stateParams.datasetUUID;
      $scope.userUid = $stateParams.userUid;
     
      DatasetResource.get($stateParams, function(response) {
        DatasetService.reset();
        DatasetService.loadStore(response.data).then(function() {
          DatasetService.queryDataset().then(function(queryResult) {
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