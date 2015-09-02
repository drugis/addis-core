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

      function oldToNew(a, b) {
        return b.idx - a.idx;
      }

      $scope.historyItems.$promise.then(function(historyResult) {
      var historyNodes = _.filter(historyResult, function(item) {
        return item['@id'].indexOf('/versions/') > 0;
      });
        var otherNodes = _.filter(historyResult, function(item) {
          return item['@id'].indexOf('/versions/') <= 0;
        });

        historyNodes = _.map(historyNodes, function(item) {
          return _.extend(item, {
            title: item['http://purl.org/dc/terms/title'],
            description: item['http://purl.org/dc/terms/description']
          });
        });

        $scope.historyItems = HistoryService.addOrderIndex(historyNodes);
        $scope.historyItems.sort(oldToNew);
        $scope.historyItems = HistoryService.addMergeIndicators($scope.historyItems, historyResult);
        var headItemId = ($scope.historyItems[0])['@id'];
        $scope.headVersion = headItemId.substr(headItemId.lastIndexOf('/') + 1);
      });
    };
    return dependencies.concat(DatasetHistoryController);
  });
