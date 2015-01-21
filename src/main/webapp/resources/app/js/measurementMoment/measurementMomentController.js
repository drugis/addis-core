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

      $scope.periodTypeOptions = [{
        value: 'H',
        type: 'time',
        label: 'hour(s)'
      }, {
        value: 'D',
        type: 'day',
        label: 'day(s)'
      }, {
        value: 'W',
        type: 'day',
        label: 'week(s)'
      }];

      $scope.itemCache = {
        duration: {}
      };

     EpochService.queryItems($stateParams.studyUUID).then(function(queryResult) {
        $scope.epochs = queryResult.data.results.bindings;
      });

     $scope.isValidDuration = function(duration) {
      return DurationService.isValidDuration(duration);
     };

      $scope.addItem = function() {
        MeasurementMomentService.addItem($scope.itemCache)
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
