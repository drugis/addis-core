'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modalInstance', 'EndpointService', 'MeasurementMomentService', 'callback'];
    var addEndpointController = function($scope, $modalInstance, EndpointService, MeasurementMomentService, callback) {

      $scope.item = {
        measuredAtMoments: []
      };

      $scope.measurementMoments = MeasurementMomentService.queryItems();

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
