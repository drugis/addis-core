'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'EpochService', 'callback', 'DurationService'];
    var addEpochController = function($scope, $modalInstance, EpochService, callback, DurationService) {

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

      $scope.addItem = function() {
        EpochService.addItem($scope.item)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create epoch');
              $modalInstance.dismiss('cancel');
            });

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };

      $scope.changeToInstantaneous = function() {
        $scope.item.duration = 'PT0S';
      }
    };
    return dependencies.concat(addEpochController);
  });
