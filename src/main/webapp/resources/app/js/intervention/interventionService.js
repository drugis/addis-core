'use strict';
define(['angular', 'lodash', 'moment'], function(angular, _, moment) {
  var dependencies = [];
  var InterventionService = function() {

    var LOWER_BOUND_OPTIONS = [{
      value: 'AT_LEAST',
      label: 'At least (>=)',
      shortLabel: '>='
    }, {
      value: 'MORE_THAN',
      label: 'More than (>)',
      shortLabel: '>'
    }, {
      value: 'EXACTLY',
      label: 'Exactly (=)',
      shortLabel: '='
    }];

    var UPPER_BOUND_OPTIONS = [{
      value: 'LESS_THAN',
      label: 'Less than (<)',
      shortLabel: '<'
    }, {
      value: 'AT_MOST',
      label: 'At most (<=)',
      shortLabel: '<='
    }];

    function typeValueToObject(typeValue) {
      return _.find(UPPER_BOUND_OPTIONS.concat(LOWER_BOUND_OPTIONS), function(option) {
        return option.value === typeValue;
      });
    }

    function createUnitLabel(unitName, unitPeriod) {
      var periodLabel = moment.duration(unitPeriod).humanize();
      periodLabel = periodLabel === 'a day' ? 'day' : periodLabel;
      return unitName + '/' + periodLabel;
    }

    function addBoundsToLabel(lowerBound, upperBound) {
      var label = '';
      if (lowerBound) {
        label += typeValueToObject(lowerBound.type).shortLabel + ' ' + lowerBound.value + ' ' + createUnitLabel(lowerBound.unitName, lowerBound.unitPeriod);
      }
      if (lowerBound && upperBound) {
        label += ' AND ';
      }
      if (upperBound) {
        label += typeValueToObject(upperBound.type).shortLabel + ' ' + upperBound.value + ' ' + createUnitLabel(upperBound.unitName, upperBound.unitPeriod);
      }
      return label;
    }

    function makeMultiTreatmentLabel(intervention, interventions, joinStr) {
      var interventionsById = _.keyBy(interventions, 'id');
      return intervention.interventionIds
        .map(function(interventionId) {
          return interventionsById[interventionId].name;
        })
        .join(joinStr);
    }

    function generateDescriptionLabel(intervention, interventions) {
      var label = '';
      if (intervention.type === 'fixed' || intervention.type === 'titrated' || intervention.type === 'both') {
        label += ': ';
        label += intervention.type === 'both' ? '' : intervention.type + ' dose; ';
        if (intervention.type === 'fixed') {
          label += 'dose ';
          label += addBoundsToLabel(intervention.constraint.lowerBound, intervention.constraint.upperBound);
        } else {
          if (intervention.minConstraint) {
            label += 'min dose ';
            label += addBoundsToLabel(intervention.minConstraint.lowerBound, intervention.minConstraint.upperBound);
          }
          if (intervention.minConstraint && intervention.maxConstraint) {
            label += '; ';
          }
          if (intervention.maxConstraint) {
            label += 'max dose ';
            label += addBoundsToLabel(intervention.maxConstraint.lowerBound, intervention.maxConstraint.upperBound);
          }
        }
      } else if (intervention.type === 'combination') {
        label = makeMultiTreatmentLabel(intervention, interventions, ' + ');
      } else if (intervention.type === 'class') {
        label = makeMultiTreatmentLabel(intervention, interventions, ' OR ');
      }
      return label;
    }

    function omitEmptyBounds(constraint) {
      if (!constraint) {
        return;
      }
      var omitList = [];
      if (!constraint.lowerBound || !constraint.lowerBound.isEnabled) {
        omitList.push('lowerBound');
      }
      if (!constraint.upperBound || !constraint.upperBound.isEnabled) {
        omitList.push('upperBound');
      }
      return _.omit(constraint, omitList);
    }

    function omitEmptyConstraints(constraint, minConstraintName, maxConstraintName) {
      if (!constraint) {
        return;
      }
      var emptyConstraints = _.filter([minConstraintName, maxConstraintName], function(constraintName) {
        return _.isEmpty(constraint[constraintName]);
      });
      return _.omit(constraint, emptyConstraints);
    }

    function cleanUpBounds(createInterventionCommand) {
      var cleanedCommand = angular.copy(createInterventionCommand);

      if (cleanedCommand.type === 'fixed') {
        cleanedCommand.fixedDoseConstraint = omitEmptyBounds(cleanedCommand.fixedDoseConstraint);
      } else if (cleanedCommand.type === 'titrated') {
        cleanedCommand.titratedDoseMinConstraint = omitEmptyBounds(cleanedCommand.titratedDoseMinConstraint);
        cleanedCommand.titratedDoseMaxConstraint = omitEmptyBounds(cleanedCommand.titratedDoseMaxConstraint);
        cleanedCommand = omitEmptyConstraints(cleanedCommand, 'titratedDoseMinConstraint', 'titratedDoseMaxConstraint');
      } else if (cleanedCommand.type === 'both') {
        cleanedCommand.bothDoseTypesMinConstraint = omitEmptyBounds(cleanedCommand.bothDoseTypesMinConstraint);
        cleanedCommand.bothDoseTypesMaxConstraint = omitEmptyBounds(cleanedCommand.bothDoseTypesMaxConstraint);
        cleanedCommand = omitEmptyConstraints(cleanedCommand, 'bothDoseTypesMinConstraint', 'bothDoseTypesMaxConstraint');
      }
      return cleanedCommand;
    }

    return {
      LOWER_BOUND_OPTIONS: LOWER_BOUND_OPTIONS,
      UPPER_BOUND_OPTIONS: UPPER_BOUND_OPTIONS,
      generateDescriptionLabel: generateDescriptionLabel,
      cleanUpBounds: cleanUpBounds
    };
  };
  return dependencies.concat(InterventionService);
});
