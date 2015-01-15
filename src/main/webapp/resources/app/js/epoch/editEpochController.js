'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback'];
    var EditEpochController = function($scope, $state, $modalInstance, itemService, callback) {
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

      var itemCache = angular.copy($scope.item);
      itemCache.isPrimary.value = itemCache.isPrimary.value === 'true';
      itemCache.duration = itemService.transformDuration(itemCache.duration);

      if(itemCache.duration.durationType === 'instantaneous') {
        itemCache.duration.numberOfPeriods = 1;
        itemCache.duration.periodType = $scope.periodTypeOptions[0];
      }
      $scope.itemCache = itemCache;

      $scope.isValidDuration = itemService.isValidDuration;

      $scope.editItem = function() {
        $scope.itemCache.duration.periodType = _.find($scope.periodTypeOptions, function(option){
          return option.value === $scope.itemCache.duration.periodType.value;
        });
        itemService.editItem($scope.item, $scope.itemCache, $state.params.studyUUID).then(function() {
            $scope.item = angular.copy($scope.itemCache);
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
