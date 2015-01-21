'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback', 'DurationService'
    ];
    var EditEpochController = function($scope, $state, $modalInstance,
      itemService, callback, DurationService) {

      var itemScratch = angular.copy($scope.item);
      itemScratch.isPrimary.value = itemScratch.isPrimary.value === 'true';

      $scope.itemScratch = itemScratch;

      $scope.durationType = itemScratch.duration.value === 'PT0S' ? 'instantaneous' : 'period';

      $scope.isValidDuration = DurationService.isValidDuration;

      $scope.changeToDuration = function() {
        if($scope.itemScratch.duration.value === 'PT0S') {
          $scope.itemScratch.duration.value = 'PT1H';
        }
      };

      $scope.changeToInstantaneous = function() {
        $scope.itemScratch.duration.value = 'PT0S';
      }

      $scope.editItem = function() {
        itemService.editItem($scope.item, $scope.itemScratch, $state.params.studyUUID).then(function() {
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
    return dependencies.concat(EditEpochController);
  });
