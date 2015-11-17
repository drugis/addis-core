'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$modalInstance',
      'callback',
      'actionType',
      'MeasurementMomentService',
      'EpochService',
      'DurationService'
    ];
    var MeasurementMomentController = function($scope, $modalInstance, callback, actionType,
      MeasurementMomentService, EpochService, DurationService) {

      var epochsPromise = EpochService.queryItems().then(function(queryResult) {
        $scope.epochs = _.sortBy(queryResult, 'pos');
        $scope.itemScratch.epoch = _.find($scope.epochs, function(epoch) {
          return $scope.itemScratch.epochUri === epoch.uri;
        });
      });

      $scope.addItem = function() {
        MeasurementMomentService.addItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to create measurement moment');
              $modalInstance.dismiss('cancel');
            });
      };

      $scope.editItem = function() {
        MeasurementMomentService.editItem($scope.itemScratch)
          .then(function() {
              callback();
              $modalInstance.close();
            },
            function() {
              console.error('failed to edit measurement moment');
              $modalInstance.dismiss('cancel');
            });
      };

      $scope.actionType = actionType;

      if (actionType === 'Add') {
        $scope.hasOffset = 'false';
        $scope.itemScratch = {
          offset: 'PT0S',
          relativeToAnchor: 'ontology:anchorEpochStart'
        };

        $scope.commit = $scope.addItem;
      } else {
        $scope.itemScratch = angular.copy($scope.item);
        $scope.hasOffset = $scope.itemScratch.offset === 'PT0S' ? 'false' : 'true';

        $scope.commit = $scope.editItem;
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

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      }
    };
    return dependencies.concat(MeasurementMomentController);
  });
