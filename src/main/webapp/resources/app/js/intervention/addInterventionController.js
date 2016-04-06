'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$modalInstance', 'callback', 'InterventionResource'];
  var AddInterventionController = function($scope, $modalInstance, callback, InterventionResource) {

    var deregisterConstraintWatch;

    $scope.checkForDuplicateInterventionName = checkForDuplicateInterventionName;
    $scope.cancel = cancel;
    $scope.addIntervention = addIntervention;
    $scope.selectTab = selectTab;
    $scope.cleanUpBounds = cleanUpBounds;

    $scope.newIntervention = {
      doseType: 'simple'
    };
    $scope.duplicateInterventionName = {
      isDuplicate: false
    };
    $scope.isAddingIntervention = false;
    $scope.activeTab = 'simple';

    function flattenTypes(newIntervention) {
      newIntervention.fixedDoseConstraint = flattenType(newIntervention.fixedDoseConstraint);
      newIntervention.titratedDoseMinConstraint = flattenType(newIntervention.titratedDoseMinConstraint);
      newIntervention.titratedDoseMaxConstraint = flattenType(newIntervention.titratedDoseMaxConstraint);
      newIntervention.bothDoseTypesMinConstraint = flattenType(newIntervention.bothDoseTypesMinConstraint);
      newIntervention.bothDoseTypesMaxConstraint = flattenType(newIntervention.bothDoseTypesMaxConstraint);
      return newIntervention;
    }

    function flattenType(constraint) {
      if(!constraint) {
        return undefined;
      }
      if(constraint.lowerBound) {
        constraint.lowerBound.type = constraint.lowerBound.type.value;
      }
      if(constraint.upperBound) {
        constraint.upperBound.type = constraint.upperBound.type.value;
      }
      return constraint;
    }

    function addIntervention(newIntervention) {
      var unitCache = newIntervention.unit;
      newIntervention.unitName = unitCache.unitName;
      newIntervention.unitPeriod = unitCache.unitPeriod;
      $scope.isAddingIntervention = true;
      newIntervention.projectId = $scope.project.id;
      newIntervention.semanticInterventionLabel = newIntervention.semanticIntervention.label;
      newIntervention.semanticInterventionUuid = newIntervention.semanticIntervention.uri;

      delete newIntervention.semanticIntervention;
      newIntervention = flattenTypes(newIntervention); // go from object with label to value only
      InterventionResource.save(newIntervention, function() {
        $modalInstance.close();
        callback(newIntervention);
        $scope.isAddingIntervention = false;
      });
    }

    function checkForDuplicateInterventionName(name) {
      $scope.duplicateInterventionName.isDuplicate = _.find($scope.interventions, function(item) {
        return item.name === name;
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }

    function cleanUpBounds() {
      if ($scope.newIntervention.doseType === 'fixed') {
        delete $scope.newIntervention.titratedDoseMinConstraint;
        delete $scope.newIntervention.titratedDoseMaxConstraint;
        delete $scope.newIntervention.bothDoseTypesMinConstraint;
        delete $scope.newIntervention.bothDoseTypesMaxConstraint;
      } else if ($scope.newIntervention.doseType === 'titrated') {
        delete $scope.newIntervention.fixedDoseConstraint;
        delete $scope.newIntervention.bothDoseTypesMinConstraint;
        delete $scope.newIntervention.bothDoseTypesMaxConstraint;
      } else if ($scope.newIntervention.doseType === 'both') {
        delete $scope.newIntervention.fixedDoseConstraint;
        delete $scope.newIntervention.titratedDoseMinConstraint;
        delete $scope.newIntervention.titratedDoseMaxConstraint;
      }
    }

    function isNumeric(n) {
      return !isNaN(parseFloat(n)) && isFinite(n);
    }

    function isMissingBoundValue(bound) {
      return !bound.type || !bound.unit || !isNumeric(bound.value);
    }

    function isIncompleteConstraint(constraint) {
      if (constraint.lowerBound && isMissingBoundValue(constraint.lowerBound)) {
        return true;
      }
      if (constraint.upperBound && isMissingBoundValue(constraint.upperBound)) {
        return true;
      }
      return false;
    }

    function checkConstraints() {
      var nonNullConstraints = _.compact([$scope.newIntervention.fixedDoseConstraint,
        $scope.newIntervention.titratedDoseMinConstraint,
        $scope.newIntervention.titratedDoseMaxConstraint,
        $scope.newIntervention.bothDoseTypesMinConstraint,
        $scope.newIntervention.bothDoseTypesMaxConstraint
      ]);
      var nonEmptyConstraints = nonNullConstraints.filter(function(constraint) {
        return constraint.lowerBound || constraint.upperBound;
      });

      $scope.hasIncorrectConstraints = nonEmptyConstraints.length === 0 || _.find(nonEmptyConstraints, isIncompleteConstraint);
    }

    function selectTab(selectedTab) {
      $scope.newIntervention = {};
      $scope.activeTab = selectedTab;
      if (selectedTab === 'dose-restricted') {
        deregisterConstraintWatch = $scope.$watch('newIntervention', checkConstraints, true);
      } else {
        $scope.newIntervention.doseType = 'simple';
        delete $scope.correctConstraints;
        deregisterConstraintWatch();
      }
    }

  };
  return dependencies.concat(AddInterventionController);
});
