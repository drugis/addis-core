'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'ArmService', 'callback'];
    var CreateArmController = function($scope, $state, $modalInstance, ArmService, callback) {
      $scope.item = {}; // necessary to make html bindings not go to parent scope

      $scope.createArm = function () {
        ArmService.addItem($scope.item, $scope.studyUuid).then(function() {
          callback();
          $modalInstance.close();
        },
        function() {
          console.error('failed to add arm');
          $modalInstance.close();
        });

      };

      $scope.cancel = function() {
        $modalInstance.close();
      };
    };
    return dependencies.concat(CreateArmController);
  });
