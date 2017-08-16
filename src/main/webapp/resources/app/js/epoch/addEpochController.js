'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'EpochService', 'callback', 'DurationService'];
    var addEpochController = function($scope, $modalInstance, EpochService, callback, DurationService) {
      // functions
      $scope.addItem = addItem;
      $scope.cancel = cancel;
      $scope.changeToInstantaneous = changeToInstantaneous;

      // init
      $scope.periodTypeOptions = [{
        value: 'H',
        type: 'time',
        label: 'hour(s)'
      }, {
        value: 'D',
        type: 'day',
        label: 'day(s)'
      }, {
        value: 'W',
        type: 'day',
        label: 'week(s)'
      }];

      $scope.item = {
        duration: {
          numberOfPeriods: 1,
          periodType: $scope.periodTypeOptions[0]
        }
      };

      $scope.isValidDuration = DurationService.isValidDuration;

      function addItem() {
        EpochService.addItem($scope.item)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create epoch');
              $modalInstance.dismiss('cancel');
            });
      }

      function cancel() {
        $modalInstance.dismiss('cancel');
      }

      function changeToInstantaneous() {
        $scope.item.duration = 'PT0S';
      }
    };
    return dependencies.concat(addEpochController);
  });