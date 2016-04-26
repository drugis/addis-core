'use strict';
define(['lodash', 'angular'], function(_, angular) {
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
      if (!constraint) {
        return undefined;
      }
      if (constraint.lowerBound) {
        constraint.lowerBound.type = constraint.lowerBound.type.value;
        constraint.lowerBound.unitName = constraint.lowerBound.unit.unitName;
        constraint.lowerBound.unitPeriod = constraint.lowerBound.unit.unitPeriod;
        constraint.lowerBound.unitConcept = constraint.lowerBound.unit.unitConcept;
        delete constraint.lowerBound.unit;
      }
      if (constraint.upperBound) {
        constraint.upperBound.type = constraint.upperBound.type.value;
        constraint.upperBound.unitName = constraint.upperBound.unit.unitName;
        constraint.upperBound.unitPeriod = constraint.upperBound.unit.unitPeriod;
        constraint.upperBound.unitConcept = constraint.upperBound.unit.unitConcept;
        delete constraint.upperBound.unit;
      }
      return constraint;
    }

    function addIntervention(newIntervention) {
      $scope.isAddingIntervention = true;
      var createCommand = buildCreateInterventionCommand(newIntervention);
      InterventionResource.save(createCommand, function(intervention) {
        $modalInstance.close();
        callback(intervention);
        $scope.isAddingIntervention = false;
      });
    }

    function buildCreateInterventionCommand(newIntervention) {
      var createInterventionCommand = angular.copy(newIntervention);

      createInterventionCommand.projectId = $scope.project.id;
      createInterventionCommand.semanticInterventionLabel = newIntervention.semanticIntervention.label;
      createInterventionCommand.semanticInterventionUuid = newIntervention.semanticIntervention.uri;
      delete createInterventionCommand.semanticIntervention;

      createInterventionCommand = flattenTypes(createInterventionCommand); // go from object with label to value only

      createInterventionCommand = cleanUpConstaints(createInterventionCommand);

      return createInterventionCommand;
    }

    /*
    ** remove constraints from the command if no bounds are set
    */
    function cleanUpConstaints(createInterventionCommand) {
      var cleanedCommand = angular.copy(createInterventionCommand);

      if (createInterventionCommand.doseType === 'both') {
        if (!createInterventionCommand.bothDoseTypesMinConstraint.lowerBound &&
          !createInterventionCommand.bothDoseTypesMinConstraint.upperBound) {
          delete cleanedCommand.bothDoseTypesMinConstraint;
        }
        if (!createInterventionCommand.bothDoseTypesMaxConstraint.lowerBound &&
          !createInterventionCommand.bothDoseTypesMaxConstraint.upperBound) {
          delete cleanedCommand.bothDoseTypesMaxConstraint;
        }
      } else if (createInterventionCommand.doseType === 'titrated') {
        if (!createInterventionCommand.titratedDoseMinConstraint.lowerBound &&
          !createInterventionCommand.titratedDoseMinConstraint.upperBound) {
          delete cleanedCommand.titratedDoseMinConstraint;
        }
        if (!createInterventionCommand.titratedDoseMaxConstraint.lowerBound &&
          !createInterventionCommand.titratedDoseMaxConstraint.upperBound) {
          delete cleanedCommand.titratedDoseMaxConstraint;
        }
      }

      return cleanedCommand;
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
