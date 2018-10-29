'use strict';
define([],
  function() {
    var dependencies = [
      '$scope',
      '$modalInstance',
      'itemService',
      'callback'
    ];
    var EditCategoryItemController = function(
      $scope,
      $modalInstance,
      itemService,
      callback
    ) {
      $scope.editItem = editItem;
      $scope.cancel = cancel;

      function editItem() {
        itemService.editItem($scope.item)
          .then(succesCallback, errorCallback);
      }

      function succesCallback() {
        callback();
        $modalInstance.close();
      }

      function errorCallback() {
        $modalInstance.close();
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditCategoryItemController);
  });
