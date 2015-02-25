'use strict';
define([],
  function() {
    var dependencies = [
      '$scope',
      '$stateParams',
      '$modalInstance',
      'callback',
      'actionType',
      'ActivityService'
    ];
    var ActivityController = function($scope, $stateParams, $modalInstance, callback, actionType, ActivityService) {

      $scope.actionType = actionType;

      if ($scope.actionType === 'Add') {
        $scope.itemScratch = {};
        $scope.commit = $scope.addItem;
      } else {
        $scope.itemScratch = angular.copy($scope.item);
        $scope.commit = $scope.editItem;
      }

      var activitiesQueryPromise = ActivityService.queryItems($stateParams.studyUUID).then(function(result) {
        $scope.activities = result;
      });

      $scope.addItem = function() {
        ActivityService.addItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create activity');
              $modalInstance.dismiss('cancel');
            });
      };

      $scope.editItem = function() {
        ActivityService.editItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to edit activity');
              $modalInstance.dismiss('cancel');
            });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      }

    };
    return dependencies.concat(ActivityController);
  });
