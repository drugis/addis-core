'use strict';
define([],
  function() {
var dependencies = ['$scope', '$stateParams', 'HistoryResource', 'HistoryService'];
  var DatasetHistoryController = function($scope, $stateParams, HistoryResource, HistoryService) {
    $scope.historyItems = HistoryResource.get($stateParams);

    $scope.historyItems.$promise.then(function(historyResult) {
      $scope.historyItems = HistoryService.addOrderIndex($scope.historyItems);
    });
  };
  return dependencies.concat(DatasetHistoryController);
});
