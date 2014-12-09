'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'StudyService', 'successCallback'];
    var CreateArmController = function($scope, $state, $modalInstance, StudyService, successCallback) {
      $scope.arm = {};

      $scope.createArm = function () {
        StudyService.addArm($scope.arm).then(function() {
          console.log('arm ' + $scope.arm + 'create');
          successCallback();
          $modalInstance.close();
        },
        function() {
          console.error('failed to add arm');
          $modalInstance.dismiss('cancel');
        });
         
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(CreateArmController);
  });
