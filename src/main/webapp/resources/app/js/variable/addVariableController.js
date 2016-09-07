'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$injector', '$modalInstance', 'MeasurementMomentService', 'ResultsService', 'callback', 'settings'];
  var addVariableController = function($scope, $injector, $modalInstance, MeasurementMomentService, ResultsService, callback, settings) {
    var service = $injector.get(settings.service);
    $scope.settings = settings;
    $scope.item = {
      measuredAtMoments: [],
      resultProperties: [],
      measurementType: 'ontology:dichotomous'
    };
    $scope.measurementMoments = MeasurementMomentService.queryItems();
    $scope.resultProperties = ResultsService.VARIABLE_TYPE_DETAILS;
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
    $scope.addItem = addItem;
    $scope.resetResultProperties = resetResultProperties;
    $scope.measurementMomentEquals = function(moment1, moment2) {
      return moment1.uri === moment2.uri;
    };
    resetResultProperties();

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
