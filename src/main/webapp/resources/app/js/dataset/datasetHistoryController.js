'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'HistoryResource', 'DatasetResource'];
    var DatasetHistoryController = function($scope, $stateParams, HistoryResource, DatasetResource) {

      $scope.datasetUuid = $stateParams.datasetUuid;
      $scope.userUid = $stateParams.userUid;

      function getDataset() {
        DatasetResource.getForJson($stateParams).$promise.then(function(response) {
          $scope.dataset = {
            datasetUri: $scope.datasetUuid,
            label: response['http://purl.org/dc/terms/title'] || response.title,
            comment: response['http://purl.org/dc/terms/description'] || response.description,
            creator: response['http://purl.org/dc/terms/creator'] || response.creator
          };
          $scope.dataset.uuid = $stateParams.datasetUuid;
        });
      }
      getDataset();

      $scope.historyItems = HistoryResource.query($stateParams);
      $scope.historyItems.$promise.then(function() {
        function oldToNew(a, b) {
          return b.i - a.i;
        }
        $scope.historyItems.sort(oldToNew);
        var headItemUri = ($scope.historyItems[0].uri);
        $scope.headVersion = headItemUri.substr(headItemUri.lastIndexOf('/') + 1);
      });
    };
    return dependencies.concat(DatasetHistoryController);
  });
