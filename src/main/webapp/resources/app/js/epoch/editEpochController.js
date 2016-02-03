'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback', 'DurationService', 'item'
    ];
    var EditEpochController = function($scope, $state, $modalInstance,
      itemService, callback, DurationService, item) {

      var itemScratch = item;
      itemScratch.isPrimary = itemScratch.isPrimary === true;

      $scope.itemScratch = itemScratch;

      $scope.durationType = itemScratch.duration === 'PT0S' ? 'instantaneous' : 'period';

      $scope.isValidDuration = DurationService.isValidDuration;

      $scope.changeToDuration = function() {
        if($scope.itemScratch.duration === 'PT0S') {
          $scope.itemScratch.duration = 'P1W';
        }
      };

      $scope.changeToInstantaneous = function() {
        $scope.itemScratch.duration = 'PT0S';
      }

      $scope.editItem = function() {
        itemService.editItem($scope.itemScratch).then(function() {
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
