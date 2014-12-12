'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'PopulationCharacteristicService', 'successCallback'];
    var CreatePopulationCharacteristicController = function($scope, $modalInstance, PopulationCharacteristicService, successCallback) {

      $scope.item = {};

      $scope.createPopulationCharacteristic = function() {
        PopulationCharacteristicService.addItem($scope.item)
          .then(function() {
              successCallback();
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
