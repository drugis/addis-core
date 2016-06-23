'use strict';
define(['lodash', 'moment'], function(_, moment) {
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
        var interventionsById = _.keyBy(interventions, 'id');
        label = intervention.singleInterventionIds
          .map(function(interventionId) {
            return interventionsById[interventionId].name;
          })
          .join(' + ');
      }
      return label;
    }

    return {
      LOWER_BOUND_OPTIONS: LOWER_BOUND_OPTIONS,
      UPPER_BOUND_OPTIONS: UPPER_BOUND_OPTIONS,
      generateDescriptionLabel: generateDescriptionLabel
    };
  };
  return dependencies.concat(InterventionService);
});
