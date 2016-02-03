'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback'];
    var EditCategoryItemController = function($scope, $state, $modalInstance, itemService, callback) {

      $scope.editItem = function () {
        itemService.editItem($scope.item).then(function() {
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
    return dependencies.concat(EditCategoryItemController);
  });
