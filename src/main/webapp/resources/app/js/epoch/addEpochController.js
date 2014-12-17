'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'EpochService', 'successCallback'];
    var addEpochController = function($scope, $modalInstance, EpochService, successCallback) {

      $scope.item = {};

      $scope.addItem = function() {
        EpochService.addItem($scope.item)
          .then(function() {
              successCallback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create epoch');
              $modalInstance.dismiss('cancel');
            });

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(addEpochController);
  });
