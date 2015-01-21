'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$stateParams',
      '$modalInstance',
      'successCallback',
      'MeasurementMomentService',
      'EpochService',
      'DurationService'
    ];
    var MeasurementMomentController = function($scope,
      $stateParams, $modalInstance, successCallback,
      MeasurementMomentService, EpochService, DurationService) {

      $scope.hasOffset = 'false';

      $scope.itemScratch = {
        offset: 'PT0S'
      };

      EpochService.queryItems($stateParams.studyUUID).then(function(queryResult) {
        $scope.epochs = queryResult.data.results.bindings;
      });

      $scope.isValidDuration = function(duration) {
        return DurationService.isValidDuration(duration);
      };

      $scope.generateLabel = MeasurementMomentService.generateLabel;

      $scope.$watch($scope.itemScratch.duration, function() {
        $scope.itemScratch.label = MeasurementMomentService.generateLabel($scope.itemScratch);
      })

      $scope.addItem = function() {
        MeasurementMomentService.addItem($scope.itemScratch)
          .then(function() {
              successCallback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create epoch');
              $modalInstance.dismiss('cancel');
            });
      };
    };
    return dependencies.concat(MeasurementMomentController);
  });
