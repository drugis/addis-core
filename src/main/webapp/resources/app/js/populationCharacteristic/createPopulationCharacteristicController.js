'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'PopulationCharacteristicService', 'MeasurementMomentService', 'callback'];
    var CreatePopulationCharacteristicController = function($scope, $modalInstance, PopulationCharacteristicService, MeasurementMomentService, callback) {
      // functions
      $scope.createPopulationCharacteristic = createPopulationCharacteristic;
      $scope.cancel = cancel;

      // init
      $scope.item = {
        measuredAtMoments: []
      };
      $scope.measurementMoments = MeasurementMomentService.queryItems();

      function createPopulationCharacteristic() {
        PopulationCharacteristicService.addItem($scope.item)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create populationCharacteristic');
              $modalInstance.dismiss('cancel');
            });

      }

      function cancel() {
        $modalInstance.dismiss('cancel');
      }
    };
    return dependencies.concat(CreatePopulationCharacteristicController);
  });