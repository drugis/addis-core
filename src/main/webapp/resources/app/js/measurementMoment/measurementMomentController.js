'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$stateParams',
      '$modalInstance',
      'callback',
      'MeasurementMomentService',
      'EpochService',
      'DurationService'
    ];
    var MeasurementMomentController = function($scope,
      $stateParams, $modalInstance, callback,
      MeasurementMomentService, EpochService, DurationService) {

      var epochsPromise = EpochService.queryItems($stateParams.studyUUID).then(function(queryResult) {
        $scope.epochs = queryResult;
        $scope.itemScratch.epoch = _.find($scope.epochs, function(epoch) {
          return $scope.itemScratch.epochUri === epoch.uri;
        });
      });

      if (!$scope.item) {
        $scope.hasOffset = 'false';
        $scope.itemScratch = {
          offset: 'PT0S'
        };
      } else {
        $scope.itemScratch = angular.copy($scope.item);
        $scope.hasOffset = $scope.itemScratch.offset === 'PT0S' ? 'false' : 'true';
      }


      $scope.isValidDuration = function(duration) {
        return DurationService.isValidDuration(duration);
      };

      $scope.generateLabel = MeasurementMomentService.generateLabel;

      $scope.$watch('itemScratch.offset', function() {
        epochsPromise.then(function() {
          $scope.itemScratch.label = MeasurementMomentService.generateLabel($scope.itemScratch);
        });
      });

      $scope.addItem = function() {
        MeasurementMomentService.addItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create epoch');
              $modalInstance.dismiss('cancel');
            });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      }
    };
    return dependencies.concat(MeasurementMomentController);
  });
