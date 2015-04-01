'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'HistoryResource', 'HistoryService', 'DatasetResource', 'DatasetService'];
    var DatasetHistoryController = function($scope, $stateParams, HistoryResource, HistoryService, DatasetResource, DatasetService) {
     
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
      });
    };
    return dependencies.concat(DatasetHistoryController);
  });