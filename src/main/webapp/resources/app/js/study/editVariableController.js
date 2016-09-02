'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$state', '$modalInstance',
    'itemService', 'MeasurementMomentService', 'ResultsService',
    'callback', 'item', 'itemType'
  ];
  var EditItemController = function($scope, $state, $modalInstance,
    itemService, MeasurementMomentService, ResultsService,
    callback, item, itemType) {

    $scope.isEditing = false;
    $scope.item = item;
    $scope.itemType = itemType;
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.resultProperties = _.map(ResultsService.VARIABLE_TYPE_DETAILS, _.identity);
    item.selectedResultProperties = _.filter($scope.resultProperties, function(resultProperty){
      return _.includes(item.resultProperties, resultProperty.uri);
    });
    $scope.resultPropertyEquals = function(moment1, moment2) {
      return moment1.uri === moment2.uri;
    };
    $scope.measurementMomentEquals = function(moment1, moment2) {
      return moment1.uri === moment2.uri;
    };

    $scope.editItem = function() {
      $scope.isEditing = false;
      item.resultProperties = _.map(item.selectedResultProperties, 'uri');
      delete item.selectedResultProperties;
      itemService.editItem($scope.item).then(function() {
          callback();
          $modalInstance.close();
        },
        function() {
          $modalInstance.dismiss('cancel');
        });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(EditItemController);
});
