'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modalInstance', 'EpochService', 'successCallback', 'DurationService'];
    var addEpochController = function($scope, $stateParams, $modalInstance, EpochService, successCallback, DurationService) {

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
        EpochService.addItem($scope.item, $stateParams.studyUUID)
          .then(function() {
              successCallback();
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
    };
    return dependencies.concat(addEpochController);
  });
