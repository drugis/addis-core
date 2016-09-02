'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$injector', '$modalInstance', 'MeasurementMomentService', 'ResultsService',  'callback', 'settings'];
  var addVariableController = function($scope, $injector, $modalInstance, MeasurementMomentService, ResultsService, callback, settings) {
    var service = $injector.get(settings.service);
    $scope.settings = settings;
    $scope.item = {
      measuredAtMoments: [],
      selectedResultProperties: []
    };
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.resultProperties = _.map(ResultsService.VARIABLE_TYPE_DETAILS, _.identity);
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
    $scope.addItem = addItem;
    $scope.resetResultProperties = resetResultProperties;
    $scope.resultPropertyEquals = function(moment1, moment2) {
      return moment1.uri === moment2.uri;
    };
    function resetResultProperties() {
      $scope.item.selectedResultProperties = ResultsService.getDefaultResultProperties($scope.item.measurementType);
    }

    function addItem() {
      $scope.item.resultProperties = _.map($scope.item.selectedResultProperties, 'uri');
      service.addItem($scope.item)
        .then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            console.error('failed to create ' + settings.itemName);
            $modalInstance.dismiss('cancel');
          });
    }

  };
  return dependencies.concat(addVariableController);
});
