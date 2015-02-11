'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'ArmService', 'callback'];
    var CreateArmController = function($scope, $state, $modalInstance, ArmService, callback) {
      $scope.item = {};

      $scope.createArm = function () {
        ArmService.addItem($scope.item, $state.params.studyUUID).then(function() {
          console.log('arm ' + $scope.item + 'create');
          callback();
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
