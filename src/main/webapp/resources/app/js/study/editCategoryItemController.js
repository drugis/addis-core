'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback'];
    var EditCategoryItemController = function($scope, $state, $modalInstance, itemService, callback) {
      $scope.editItem = editItem;
      $scope.cancel = cancel;

      function editItem() {
        itemService.editItem($scope.item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditCategoryItemController);
  });