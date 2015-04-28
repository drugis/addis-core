'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'MeasurementMomentService', 'callback'];
    var EditItemController = function($scope, $state, $modalInstance, itemService, MeasurementMomentService, callback) {

      $scope.measurementMoments = MeasurementMomentService.queryItems();

      $scope.editItem = function () {
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
