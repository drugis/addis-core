'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'HistoryResource', 'DatasetResource'];
    var DatasetHistoryController = function($scope, $stateParams, HistoryResource, DatasetResource) {

      $scope.datasetUUID = $stateParams.datasetUUID;
      $scope.userUid = $stateParams.userUid;

      function getDataset() {
        DatasetResource.getForJson($stateParams).$promise.then(function(response) {
          $scope.dataset = {
            datasetUri: $scope.datasetUUID,
            label: response['http://purl.org/dc/terms/title'],
            comment: response['http://purl.org/dc/terms/description'],
            creator: response['http://purl.org/dc/terms/creator']
          };
          $scope.dataset.uuid = $stateParams.datasetUUID;
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
