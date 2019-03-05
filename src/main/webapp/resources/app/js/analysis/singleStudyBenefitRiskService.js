'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var SingleStudyBenefitRiskService = function() {

    function isSameIntervention(studyInterventionUri, selectedIntervention) {
      return selectedIntervention.semanticInterventionUri === studyInterventionUri;
    }

    function addMissingOutcomesToStudies(studies, outcomes) {
      return studies.map(function(study) {
        var updatedStudy = _.cloneDeep(study);
        updatedStudy.missingOutcomes = findMissingOutcomes(updatedStudy, outcomes);
        return updatedStudy;
      });
    }

    function findMissingOutcomes(study, outcomes) {
      return _.filter(outcomes, function(outcome) {
        return hasNoMatchingArm(outcome, study);
      });
    }

    function hasNoMatchingArm(outcome, study) {
      return !_.some(study.arms, function(arm) {
        var measurements = arm.measurements[study.defaultMeasurementMoment];
        return isValidArmForOutcome(measurements, outcome);
      });
    }

    function isValidArmForOutcome(measurements, outcome) {
      return _.some(measurements, function(measurement) {
        return measurement.variableConceptUri === outcome.outcome.semanticOutcomeUri;
      });
    }

    function addMissingInterventionsToStudies(studies, selectedInterventions) {
      return studies.map(function(study) {
        study.missingInterventions = findMissingInterventions(selectedInterventions, study.arms);
        return study;
      });
    }

    function findMissingInterventions(selectedInterventions, arms) {
      return _.filter(selectedInterventions, function(selectedIntervention) {
        return noArmMatchingIntervention(selectedIntervention, arms);
      });
    }

    function noArmMatchingIntervention(intervention, arms) {
      return !_.some(arms, function(arm) {
        return arm.matchedProjectInterventionIds.indexOf(intervention.id) > -1;
      });
    }

    function addOverlappingInterventionsToStudies(studies, selectedInterventions) {
      return _.map(studies, function(study) {
        study.overlappingInterventions = findOverlappingIntervention(selectedInterventions, study);
        return study;
      });
    }

    // Add a 'group' property for sorting alphabetically within groups while placing the 'valid' group on top of the options list
    function recalculateGroup(studies, interventions, outcome) {
      return _.map(studies, function(study) {
        var modifiedStudy = {};
        if (isValidStudyOption(study, interventions, outcome)) {
          modifiedStudy.group = 0;
          modifiedStudy.groupLabel = 'Compatible studies';
        } else {
          modifiedStudy.group = 1;
          modifiedStudy.groupLabel = 'Incompatible studies';
        }
        return _.merge({}, study, modifiedStudy);
      });
    }

    function isValidStudyOption(study, interventions, outcome) {
      var noMissingOutcomes = study.missingOutcomes ? study.missingOutcomes.length === 0 : true;
      var noMissingInterventions = study.missingInterventions ? study.missingInterventions.length === 0 : true;
      var noMixedTreatmentArm = !study.hasMatchedMixedTreatmentArm;
      var hasMissingValue = hasMissingValues(study, interventions, outcome);
      return noMissingOutcomes && noMissingInterventions && noMixedTreatmentArm && !hasMissingValue;
    }

    function hasMissingValues(study, interventions, outcome) {
      return _.find(study.arms, function(arm) {
        if (isIncludedArm(arm, interventions)) {
          var measurements = arm.measurements[study.defaultMeasurementMoment];
          if (measurements) {
            return hasMissingMeasurementsForOutcome(measurements, outcome);
          }
        }
        return false;
      });
    }

    function isIncludedArm(arm, interventions) {
      return _.some(interventions, function(intervention) {
        return _.includes(arm.matchedProjectInterventionIds, intervention.id);
      });
    }

    function hasMissingMeasurementsForOutcome(measurements, outcome) {
      return _.some(measurements, function(measurement) {
        var isMeasurementForOutcome = measurement.variableConceptUri === outcome.outcome.semanticOutcomeUri;
        if (measurement.referenceArm) {
          return isMeasurementForOutcome && !hasContrastValues(measurement);
        } else {
          return isMeasurementForOutcome && !hasAbsoluteValues(measurement);
        }
      });
    }

    function hasAbsoluteValues(measurement) {
      return (hasValue(measurement.sampleSize) && hasValue(measurement.rate)) ||
        (hasValue(measurement.sampleSize) && hasValue(measurement.stdDev) && hasValue(measurement.mean)) ||
        (hasValue(measurement.mean) && hasValue(measurement.stdErr)) ||
        (hasValue(measurement.exposure) && hasValue(measurement.rate));
    }

    function hasContrastValues(measurement) {
      return (hasValue(measurement.stdErr) && hasValue(measurement.meanDifference)) ||
        (hasValue(measurement.stdErr) && hasValue(measurement.oddsRatio)) ||
        (hasValue(measurement.stdErr) && hasValue(measurement.hazardRatio));
    }

    function hasValue(value) {
      return !isNaN(value) && value !== undefined && value !== null;
    }

    function findOverlappingIntervention(selectedInterventions, study) {
      return _.reduce(study.arms, function(accum, arm) {
        if (arm.matchedProjectInterventionIds.length > 1) {
          var matching = _.filter(selectedInterventions, function(intervention) {
            return arm.matchedProjectInterventionIds.indexOf(intervention.id) > -1;
          });
          if (matching.length > 1) {
            accum = accum.concat(matching);
          }
        }
        return accum;
      }, []);
    }

    function findAllMatchingInterventions(selectedInterventions, treatmentArm) {
      return _.filter(selectedInterventions, function(selectedIntervention) {
        return _.find(treatmentArm.interventionUids, function(interventionUid) {
          return isSameIntervention(interventionUid, selectedIntervention);
        });
      });
    }

    function addHasMatchedMixedTreatmentArm(studies, selectedInterventions) {
      return _.map(studies, function(study) {
        var modifiedStudy = {
          hasMatchedMixedTreatmentArm: _.some(study.treatmentArms, function(treatmentArm) {
            return treatmentArm.interventionUids.length > 1 && findAllMatchingInterventions(selectedInterventions, treatmentArm).length > 0;
          })
        };
        return _.merge({}, study, modifiedStudy);
      });
    }

    function getStudiesWithErrors(studies, alternatives) {
      var tempStudies = addMissingInterventionsToStudies(studies, alternatives);
      tempStudies = addHasMatchedMixedTreatmentArm(tempStudies, alternatives);
      return addOverlappingInterventionsToStudies(tempStudies, alternatives);
    }

    return {
      addOverlappingInterventionsToStudies: addOverlappingInterventionsToStudies,
      addHasMatchedMixedTreatmentArm: addHasMatchedMixedTreatmentArm,
      addMissingOutcomesToStudies: addMissingOutcomesToStudies,
      recalculateGroup: recalculateGroup,
      findMissingOutcomes: findMissingOutcomes,
      getStudiesWithErrors: getStudiesWithErrors,
      isValidStudyOption: isValidStudyOption
    };
  };
  return dependencies.concat(SingleStudyBenefitRiskService);
});
