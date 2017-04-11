'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'intervention', 'callback'];
  var RepairInterventionController = function($scope, $stateParams, $modalInstance, intervention, callback) {
    $scope.intervention = intervention;
    $scope.updateInterventionMultiplierMapping = updateInterventionMultiplierMapping;
    $scope.units = [];
    $scope.metricMultipliers = [{
      label: 'nano',
      conversionMultiplier: 1e-09
    }, {
      label: 'micro',
      conversionMultiplier: 1e-06
    }, {
      label: 'milli',
      conversionMultiplier: 1e-03
    }, {
      label: 'centi',
      conversionMultiplier: 1e-02
    }, {
      label: 'deci',
      conversionMultiplier: 1e-01
    }, {
      label: 'deca',
      conversionMultiplier: 1e01
    }, {
      label: 'hecto',
      conversionMultiplier: 1e02
    }, {
      label: 'kilo',
      conversionMultiplier: 1e03
    }, {
      label: 'mega',
      conversionMultiplier: 1e06
    }];
    buildUnits();

    function buildUnits() {
      if ($scope.intervention.type === 'fixed') {
        $scope.units.push($scope.intervention.constraint.lowerBound);
        if ($scope.intervention.constraint.upperBound) {
          $scope.units.push($scope.intervention.constraint.upperBound);
        }
      } else if ($scope.intervention.type === 'titrated') {
        $scope.units.push($scope.intervention.minConstraint.lowerBound);
        if ($scope.intervention.minConstraint.upperBound) {
          $scope.units.push($scope.intervention.minConstraint.upperBound);
        }
        $scope.units.push($scope.intervention.maxConstraint.lowerBound);
        if ($scope.intervention.maxConstraint.upperBound) {
          $scope.units.push($scope.intervention.maxConstraint.upperBound);
        }

      }
      $scope.units = _.uniqBy($scope.units, ['unitName','unitConcept']);
    }

    function updateInterventionMultiplierMapping() {
      //persist newly set conversion multipliers here

      callback();
      $modalInstance.close();
    }
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(RepairInterventionController);
});
