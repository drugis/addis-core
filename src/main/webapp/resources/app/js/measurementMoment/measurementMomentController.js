'use strict';
define(['lodash', 'angular'], function(_, angular) {
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

    $scope.isEditing = false;

    var epochsPromise = EpochService.queryItems().then(function(queryResult) {
      $scope.epochs = _.sortBy(queryResult, 'pos');
      $scope.itemScratch.epoch = _.find($scope.epochs, function(epoch) {
        return $scope.itemScratch.epochUri === epoch.uri;
      });
    });

    $scope.addItem = function() {
      $scope.isEditing = true;
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
      $scope.isEditing = true;
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

    $scope.merge = function(targetMeasurementMoment) {
      $scope.isEditing = true;
      MeasurementMomentService.merge($scope.item, targetMeasurementMoment).then(function() {
          callback();
          $modalInstance.close();
        },
        function() {
          $modalInstance.dismiss('cancel');
        });
    }

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

      MeasurementMomentService.queryItems().then(function(measurementMoments) {
        $scope.otherMeasurementMoments = _.filter(measurementMoments, function(measurementMoment) {
          return measurementMoment.uri !== $scope.itemScratch.uri;
        });
      });
      $scope.showMergeWarning = false;

    }

    $scope.updateMergeWarning = function(targetMeasurementMoment) {
      MeasurementMomentService.hasOverlap($scope.item, targetMeasurementMoment).then(function(result) {
        $scope.showMergeWarning = result;
      });
    };

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
    };
  };
  return dependencies.concat(MeasurementMomentController);
});
