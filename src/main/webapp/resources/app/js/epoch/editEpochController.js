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

      $scope.itemCache = {
        label: {
          value : $scope.item.label.value
        },
        comment: {
          value : $scope.item.comment ? $scope.item.comment.value : null
        },
        isPrimary: {
          value : $scope.item.isPrimary.value === 'true'
        },
        uri: {
          value: $scope.item.uri.value
        },
        duration: itemService.transformDuration($scope.item.duration)
      };

      duration: itemService.transformDuration($scope.item.duration)
      
      $scope.isValidDuration = itemService.isValidDuration;

      $scope.editItem = function() {
        itemService.editItem($scope.item, $scope.itemCache, $state.params.studyUUID).then(function() {
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