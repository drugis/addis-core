'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modalInstance', 'EpochService', 'successCallback'];
    var addEpochController = function($scope, $stateParams, $modalInstance, EpochService, successCallback) {

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

      $scope.isValidEpoch = function() {
        var valid = true;
        valid = $scope.item.label && $scope.item.label.length > 0;
        valid = valid && $scope.item.duration && isValidDuration(scope.item.duration);
        return valid;
      }

      $scope.isValidDuration = function(duration, periodTypeOptions) {
        if (!duration.durationType) {
          return false;
        } else if (duration.durationType === 'instantaneous') {
          return true;
        } else if (duration.durationType === 'period') {
          var isValidType = _.find(periodTypeOptions, function(option) {
            return option.value === duration.periodType.value &&
              option.label === duration.periodType.label;
          });
          var isValidNumberOfPeriods = isNormalInteger(duration.numberOfPeriods);
            return isValidType && isValidNumberOfPeriods;
        } else {
          throw "invalid duration type";
        }
      }

      //http://stackoverflow.com/questions/10834796/validate-that-a-string-is-a-positive-integer
      function isNormalInteger(str) {
        return /^\+?(0|[1-9]\d*)$/.test(str);
      }


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
