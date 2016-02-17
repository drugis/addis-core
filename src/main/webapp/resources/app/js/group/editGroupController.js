'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback', 'item'];
    var EditGroupController = function($scope, $state, $modalInstance, itemService, callback, item) {

      $scope.item = item;

      $scope.editItem = function () {
        itemService.editItem(item).then(function() {
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
    return dependencies.concat(EditGroupController);
  });
