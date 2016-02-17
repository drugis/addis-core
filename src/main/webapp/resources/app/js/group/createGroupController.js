'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'GroupService', 'callback'];
    var CreateGroupController = function($scope, $state, $modalInstance, GroupService, callback) {
      $scope.item = {}; // necessary to make html bindings not go to parent scope

      $scope.createGroup = function () {
        GroupService.addItem($scope.item, $scope.studyUuid).then(function() {
          console.log('group ' + $scope.item + 'create');
          callback();
          $modalInstance.close();
        },
        function() {
          console.error('failed to add group');
          $modalInstance.dismiss('cancel');
        });

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(CreateGroupController);
  });
