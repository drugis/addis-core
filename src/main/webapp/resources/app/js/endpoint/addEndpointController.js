'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'EndpointService', 'successCallback'];
    var addEndpointController = function($scope, $modalInstance, EndpointService, successCallback) {

      $scope.item = {};

      $scope.addItem = function() {
        EndpointService.addItem($scope.item)
          .then(function() {
              successCallback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create endpoint');
              $modalInstance.dismiss('cancel');
            });

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(addEndpointController);
  });
