'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'ArmService', 'successCallback'];
    var EditArmController = function($scope, $state, $modalInstance, ArmService, successCallback) {

      $scope.editArm = function () {
        ArmService.editArm($scope.arm).then(function() {
          successCallback();
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
    return dependencies.concat(EditArmController);
  });
