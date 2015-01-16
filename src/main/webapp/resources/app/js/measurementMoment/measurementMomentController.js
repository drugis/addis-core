'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$stateParams',
      '$modalInstance',
      'successCallback',
      'MeasurementMomentService',
      'EpochService'
    ];
    var MeasurementMomentController = function($scope,
      $stateParams, $modalInstance, successCallback,
      MeasurementMomentService, EpochService) {

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
