'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback'
    ];
    var EditStudyInformationController = function($scope, $state, $modalInstance,
      itemService, callback) {

      var itemScratch = angular.copy($scope.item);

      $scope.itemScratch = itemScratch;

      $scope.editItem = function() {
        itemService.editItem($scope.itemScratch).then(function() {
            $scope.item = angular.copy($scope.itemScratch);
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.isValidNumberOfCenters = function(a) {
        return a === undefined || Number.isInteger(a);
      }

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditStudyInformationController);
  });
