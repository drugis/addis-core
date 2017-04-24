'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'callback', 'InterventionResource',
    'ScaledUnitResource', 'InterventionService', 'ProjectService', 'DosageService'
  ];
  var AddInterventionController = function($scope, $stateParams, $modalInstance, callback, InterventionResource,
    ScaledUnitResource, InterventionService, ProjectService, DosageService) {
    var deregisterConstraintWatch;

    $scope.checkForDuplicateInterventionName = checkForDuplicateInterventionName;
    $scope.cancel = cancel;
    $scope.addIntervention = addIntervention;
    $scope.selectTab = selectTab;
    $scope.interventionTypeSwitched = interventionTypeSwitched;
    $scope.addCombinedIntervention = addCombinedIntervention;
    $scope.addInterventionClass = addInterventionClass;
    $scope.numberOfSelectedInterventions = numberOfSelectedInterventions;

    $scope.newIntervention = {
      type: 'simple',
      projectId: $scope.project.id
    };
    $scope.duplicateInterventionName = {
      isDuplicate: false
    };
    $scope.isAddingIntervention = false;
    $scope.activeTab = 'simple';

    $scope.scaledUnits = ScaledUnitResource.query($stateParams);

    $scope.singleInterventions = _.reject($scope.interventions, function(intervention) {
      return intervention.type === 'combination' || intervention.type === 'class';
    });

    $scope.nonClassInterventions = _.reject($scope.interventions, {
      'type': 'class'
    });
    DosageService.get($stateParams.userUid, $scope.project.namespaceUid).then(function(units) {
      $scope.unitConcepts = units;
    });

    $scope.$on('scaledUnitsChanged', function() {
      ScaledUnitResource.query($stateParams).$promise.then(function(units) {
        $scope.scaledUnits = units;
      });
    });

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
        constraint.lowerBound.unitName = constraint.lowerBound.unit.name;
        constraint.lowerBound.unitPeriod = constraint.lowerBound.unitPeriod;
        constraint.lowerBound.unitConcept = constraint.lowerBound.unit.conceptUri;
        constraint.lowerBound.conversionMultiplier = constraint.lowerBound.unit.multiplier;
        delete constraint.lowerBound.unit;
      }
      if (constraint.upperBound) {
        constraint.upperBound.type = constraint.upperBound.type.value;
        constraint.upperBound.unitName = constraint.upperBound.unit.name;
        constraint.upperBound.unitPeriod = constraint.upperBound.unitPeriod;
        constraint.upperBound.unitConcept = constraint.upperBound.unit.conceptUri;
        constraint.upperBound.conversionMultiplier = constraint.upperBound.unit.multiplier;
        delete constraint.upperBound.unit;
      }
      return constraint;
    }

    function persistIntervention(createCommand) {
      InterventionResource.save(createCommand, function(intervention) {
        $modalInstance.close();
        callback(intervention);
        $scope.isAddingIntervention = false;
      });
    }

    function addIntervention(newIntervention) {
      $scope.isAddingIntervention = true;
      var createCommand = buildCreateInterventionCommand(newIntervention);
      persistIntervention(createCommand);
    }

    function addCombinedIntervention(newIntervention) {
      $scope.isAddingIntervention = true;
      var createCommand = angular.copy(newIntervention);
      createCommand.interventionIds = _.reduce(createCommand.interventionIds, function(accum, isIncluded, interventionId) {
        if (isIncluded) {
          accum.push(parseInt(interventionId));
        }
        return accum;
      }, []);
      persistIntervention(createCommand);
    }

    function addInterventionClass(newIntervention) {
      $scope.isAddingIntervention = true;
      var createCommand = angular.copy(newIntervention);
      createCommand.interventionIds = _.reduce(createCommand.interventionIds, function(accum, isIncluded, interventionId) {
        if (isIncluded) {
          accum.push(parseInt(interventionId));
        }
        return accum;
      }, []);
      persistIntervention(createCommand);
    }

    function buildCreateInterventionCommand(newIntervention) {
      var createInterventionCommand = angular.copy(newIntervention);
      createInterventionCommand.semanticInterventionLabel = newIntervention.semanticIntervention.label;
      createInterventionCommand.semanticInterventionUri = newIntervention.semanticIntervention.uri;
      delete createInterventionCommand.semanticIntervention;
      createInterventionCommand = flattenTypes(createInterventionCommand); // go from object with label to value only
      createInterventionCommand = InterventionService.cleanUpBounds(createInterventionCommand);
      return createInterventionCommand;
    }


    function checkForDuplicateInterventionName(intervention) {
      $scope.duplicateInterventionName.isDuplicate = ProjectService.checkforDuplicateName($scope.interventions, intervention);
      return $scope.duplicateInterventionName.isDuplicate;
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }

    function interventionTypeSwitched() {
      if ($scope.newIntervention.type === 'fixed') {
        delete $scope.newIntervention.titratedDoseMinConstraint;
        delete $scope.newIntervention.titratedDoseMaxConstraint;
        delete $scope.newIntervention.bothDoseTypesMinConstraint;
        delete $scope.newIntervention.bothDoseTypesMaxConstraint;
      } else if ($scope.newIntervention.type === 'titrated') {
        delete $scope.newIntervention.fixedDoseConstraint;
        delete $scope.newIntervention.bothDoseTypesMinConstraint;
        delete $scope.newIntervention.bothDoseTypesMaxConstraint;
      } else if ($scope.newIntervention.type === 'both') {
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
      $scope.newIntervention = {
        projectId: $scope.project.id
      };
      $scope.activeTab = selectedTab;
      delete $scope.correctConstraints;
      if (deregisterConstraintWatch) {
        deregisterConstraintWatch();
      }

      if (selectedTab === 'dose-restricted') {
        deregisterConstraintWatch = $scope.$watch('newIntervention', checkConstraints, true);
      } else if (selectedTab === 'simple') {
        $scope.newIntervention.type = 'simple';
        delete $scope.correctConstraints;
        if (deregisterConstraintWatch) {
          deregisterConstraintWatch();
        }
      } else if (selectedTab === 'combination') {
        $scope.newIntervention.type = 'combination';
        $scope.newIntervention.interventionIds = {};
      } else if (selectedTab === 'class') {
        $scope.newIntervention.type = 'class';
        $scope.newIntervention.interventionIds = {};
      }
    }

    function numberOfSelectedInterventions() {
      return _.filter($scope.newIntervention.interventionIds).length;
    }

  };
  return dependencies.concat(AddInterventionController);
});
