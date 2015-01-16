'use strict';
define([],
  function() {
    var dependencies = ['$scope',
      '$modalInstance',
      'successCallback',
      'MeasurementMomentService'
    ];
    var MeasurementMomentController = function($scope, $modalInstance, successCallback, MeasurementMomentService) {
      $scope.anchorMoments = [{
        uri: '<http://trials.drugis.org/ontology#anchorEpochStart>',
        label: 'from epoch start'
      }, {
        uri: '<http://trials.drugis.org/ontology#anchorEpochEnd>',
        label: 'from epoch end'
      }];

      $scope.itemCache = {
        duration: {}
      };

      $scope.addItem = function() {
        MeasurementMomentService.addItem($scope.itemCaches)
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
