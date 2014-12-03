'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'StudyService', 'successCallback'];
    var ArmController = function($scope, $state, $modalInstance, StudyService, successCallback) {
      $scope.arm = {};

      $scope.createArm = function () {
         console.log('arm ' + $scope.arm + 'create');
        $modalInstance.close();
      }

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(ArmController);
  });
