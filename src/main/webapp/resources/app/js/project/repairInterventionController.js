'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', '$q',
    'InterventionResource', 'intervention', 'callback'
  ];
  var RepairInterventionController = function($scope, $stateParams, $modalInstance, $q,
    InterventionResource, intervention, callback) {
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
        $scope.units.push({
          unitName: $scope.intervention.constraint.lowerBound.unitName,
          unitConcept: $scope.intervention.constraint.lowerBound.unitConcept,
          conversionMultiplier: $scope.intervention.constraint.lowerBound.conversionMultiplier
        });
        if ($scope.intervention.constraint.upperBound) {
          $scope.units.push({
            unitName: $scope.intervention.constraint.upperBound.unitName,
            unitConcept: $scope.intervention.constraint.upperBound.unitConcept,
            conversionMultiplier: $scope.intervention.constraint.upperBound.conversionMultiplier
          });
        }
      } else if ($scope.intervention.type === 'titrated') {
        //min
        $scope.units.push({
          unitName: $scope.intervention.minConstraint.lowerBound.unitName,
          unitConcept: $scope.intervention.minConstraint.lowerBound.unitConcept,
          conversionMultiplier: $scope.intervention.minConstraint.lowerBound.conversionMultiplier
        });
        if ($scope.intervention.minConstraint.upperBound) {
          $scope.units.push({
            unitName: $scope.intervention.minConstraint.upperBound.unitName,
            unitConcept: $scope.intervention.minConstraint.upperBound.unitConcept,
            conversionMultiplier: $scope.intervention.minConstraint.upperBound.conversionMultiplier
          });
        }
        //max
        $scope.units.push({
          unitName: $scope.intervention.maxConstraint.lowerBound.unitName,
          unitConcept: $scope.intervention.maxConstraint.lowerBound.unitConcept,
          conversionMultiplier: $scope.intervention.maxConstraint.lowerBound.conversionMultiplier
        });
        if ($scope.intervention.maxConstraint.upperBound) {
          $scope.units.push({
            unitName: $scope.intervention.maxConstraint.upperBound.unitName,
            unitConcept: $scope.intervention.maxConstraint.upperBound.unitConcept,
            conversionMultiplier: $scope.intervention.maxConstraint.upperBound.conversionMultiplier
          });
        }

      }
      $scope.units = _.uniqBy($scope.units, ['unitName', 'unitConcept']);
    }

    function updateInterventionMultiplierMapping() {
      InterventionResource.setConversionMultiplier({
        projectId: $scope.project.id,
        interventionId: $scope.intervention.id
      }, {
        multipliers: $scope.units
      }).$promise.then(function() {
        callback();
        $modalInstance.close();
      });
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(RepairInterventionController);
});
