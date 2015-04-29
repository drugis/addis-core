'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback'
    ];
    var EditPopulationInformationController = function($scope, $state, $modalInstance,
      itemService, callback) {

      var itemScratch = angular.copy($scope.item);
      itemScratch.isPrimary = itemScratch.isPrimary === 'true';

      $scope.itemScratch = itemScratch;

      $scope.editItem = function() {
        itemService.editItem($scope.item, $scope.itemScratch).then(function() {
            $scope.item = angular.copy($scope.itemScratch);
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
    return dependencies.concat(EditPopulationInformationController);
  });
