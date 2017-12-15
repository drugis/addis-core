'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'callback', 'unitConcepts', 'scaledUnits'];
  var AddScaledUnitController = function($scope, $stateParams, $modalInstance, callback, unitConcepts, scaledUnits) {
    // vars
    $scope.unitConcepts = unitConcepts;
    $scope.scaledUnits = scaledUnits;

    $scope.cancel = cancel;
    $scope.$on('scaledUnitsChanged', close);
    $scope.$on('scaledUnitCancelled', cancel);

    function close() {
      callback();
      $modalInstance.close();
    }


    function cancel() {
      $modalInstance.close();
    }

  };
  return dependencies.concat(AddScaledUnitController);
});
