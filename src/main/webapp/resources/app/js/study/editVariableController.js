'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance',
    'itemService', 'MeasurementMomentService', 'ResultsService',
    'callback', 'item', 'itemType'];
    var EditItemController = function($scope, $state, $modalInstance,
      itemService, MeasurementMomentService, ResultsService,
      callback, item, itemType) {

      $scope.isEditing = false;
      $scope.item = item;
      $scope.itemType = itemType;
      $scope.measurementMoments = MeasurementMomentService.queryItems();
      $scope.resultProperties = _.map(ResultsService.VARIABLE_TYPE_DETAILS, function(variableTypeDetail) {
        return variableTypeDetail;
      });

      $scope.editItem = function() {
        $scope.isEditing = false;
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
