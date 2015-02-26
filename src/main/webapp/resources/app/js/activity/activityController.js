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
      $scope.activityTypeOptions = _.values(ActivityService.ACTIVITY_TYPE_OPTIONS);

      $scope.addItem = function() {
        ActivityService.addItem($stateParams.studyUUID, $scope.itemScratch)
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
        ActivityService.editItem($stateParams.studyUUID, $scope.itemScratch)
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

      if ($scope.actionType === 'Add') {
        $scope.itemScratch = {};
        $scope.commit = $scope.addItem; // set the function to be called when the form is submitted
      } else {
        $scope.itemScratch = angular.copy($scope.item);
        // select from the options map as ng-select works by reference
        $scope.itemScratch.activityType = ActivityService.ACTIVITY_TYPE_OPTIONS[$scope.itemScratch.activityType.uri];
        $scope.commit = $scope.editItem;
      }

    };
    return dependencies.concat(ActivityController);
  });
