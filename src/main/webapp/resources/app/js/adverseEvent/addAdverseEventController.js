'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modalInstance', 'AdverseEventService', 'MeasurementMomentService', 'callback'];
    var addAdverseEventController = function($scope, $stateParams, $modalInstance, AdverseEventService, MeasurementMomentService, callback) {

      $scope.item = {
        measuredAtMoments: []
      };

      $scope.measurementMoments = MeasurementMomentService.queryItems($stateParams.studyUUID);

      $scope.addItem = function() {
        AdverseEventService.addItem($scope.item)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create adverseEvent');
              $modalInstance.dismiss('cancel');
            });

      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(addAdverseEventController);
  });