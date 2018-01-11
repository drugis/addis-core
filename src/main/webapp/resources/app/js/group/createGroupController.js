'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'GroupService', 'callback'];
    var CreateGroupController = function($scope, $state, $modalInstance, GroupService, callback) {
      // functions
      $scope.createGroup =createGroup;
      $scope.cancel = cancel;

      // init
      $scope.item = {}; // necessary to make html bindings not go to parent scope

       function createGroup() {
        GroupService.addItem($scope.item, $scope.studyUuid).then(function() {
          console.log('group ' + $scope.item + 'create');
          callback();
          $modalInstance.close();
        },
        function() {
          console.error('failed to add group');
          $modalInstance.close();
        });
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(CreateGroupController);
  });
