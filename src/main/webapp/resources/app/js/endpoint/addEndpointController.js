'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'EndpointService', 'callback'];
    var addEndpointController = function($scope, $modalInstance, EndpointService, callback) {

      $scope.item = {};

      $scope.addItem = function() {
        EndpointService.addItem($scope.item)
          .then(function() {
              callback();
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
