'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', 'UnitService', '$modalInstance', 'unit'];
  var RepairUnitController = function($scope, UnitService, $modalInstance, unit) {
    // functions
    $scope.merge = merge;
    $scope.cancel = cancel;

    // init
    $scope.isMerging = false;
    $scope.unit = unit;
    UnitService.queryItems().then(function(units) {
      $scope.otherUnits = _.reject(units, ['uri', $scope.unit.uri]);
    });

    function merge(mergeTarget) {
      $scope.isMerging = true;
      UnitService.merge($scope.unit, mergeTarget).then(function() {
        $scope.$emit('updateStudyDesign');
        $modalInstance.close();
      });
    }

    function cancel() {
      $modalInstance.close();
    }
  };
  return dependencies.concat(RepairUnitController);
});