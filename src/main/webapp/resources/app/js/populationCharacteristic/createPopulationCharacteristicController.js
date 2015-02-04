'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'PopulationCharacteristicService', 'callback'];
    var CreatePopulationCharacteristicController = function($scope, $modalInstance, PopulationCharacteristicService, callback) {

      $scope.item = {};

      $scope.createPopulationCharacteristic = function() {
        PopulationCharacteristicService.addItem($scope.item)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create populationCharacteristic');
              $modalInstance.dismiss('cancel');
            });

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(CreatePopulationCharacteristicController);
  });
