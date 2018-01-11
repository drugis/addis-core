'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$state', '$modalInstance',
      'itemService', 'callback', 'DurationService', 'item'
    ];
    var EditEpochController = function($scope, $state, $modalInstance,
      itemService, callback, DurationService, item) {
      // functions
      $scope.changeToDuration = changeToDuration;
      $scope.changeToInstantaneous = changeToInstantaneous;
      $scope.editItem = editItem;
      $scope.cancel = cancel;

      // init
      $scope.isEditing = false;
      var itemScratch = item;
      itemScratch.isPrimary = itemScratch.isPrimary === true;
      $scope.itemScratch = itemScratch;
      $scope.durationType = itemScratch.duration === 'PT0S' ? 'instantaneous' : 'period';
      $scope.isValidDuration = DurationService.isValidDuration;

      function changeToDuration() {
        if ($scope.itemScratch.duration === 'PT0S') {
          $scope.itemScratch.duration = 'P1W';
        }
      }

      function changeToInstantaneous() {
        $scope.itemScratch.duration = 'PT0S';
      }

      function editItem() {
        $scope.isEditing = true;
        itemService.editItem($scope.itemScratch).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditEpochController);
  });