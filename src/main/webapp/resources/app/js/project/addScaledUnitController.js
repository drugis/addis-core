'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'callback', 'unitConcepts', 'scaledUnits'];
  var AddScaledUnitController = function($scope, $stateParams, $modalInstance, callback, unitConcepts, scaledUnits) {
    // vars
    $scope.unitConcepts = unitConcepts;
    $scope.scaledUnits = scaledUnits;

    $scope.cancel = cancel;
    $scope.$on('scaledUnitsChanged', close);


    function close() {
      callback();
      $modalInstance.close();
    }


    function cancel() {
      $modalInstance.dismiss('cancel');
    }

  };
  return dependencies.concat(AddScaledUnitController);
});
