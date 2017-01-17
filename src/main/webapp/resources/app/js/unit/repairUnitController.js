'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', 'UnitService', '$modalInstance', 'unit'];
  var RepairUnitController = function($scope, UnitService, $modalInstance, unit) {
    $scope.isMerging = false;
    $scope.unit = unit;
    UnitService.queryItems().then(function(units) {
      $scope.otherUnits = _.reject(units, ['uri', $scope.unit.uri]);
    });

    $scope.merge = function(mergeTarget) {
      $scope.isMerging = true;
      UnitService.merge($scope.unit, mergeTarget).then(function() {
        $scope.$emit('updateStudyDesign');
        $modalInstance.close();
      });
    };

    $scope.cancel = function() {
      $modalInstance.close();
    };
  };
  return dependencies.concat(RepairUnitController);
});
