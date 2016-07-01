'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'MeasurementMomentService', 'callback', 'item'];
    var EditItemController = function($scope, $state, $modalInstance, itemService, MeasurementMomentService, callback, item) {

      $scope.isEditing = false;
      $scope.measurementMoments = MeasurementMomentService.queryItems();
      $scope.item = item;

      $scope.editItem = function() {
        $scope.isEditing = true;
        itemService.editItem(item).then(function() {
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
