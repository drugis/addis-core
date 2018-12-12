'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var SingleStudyBenefitRiskService = function() {

    function isSameIntervention(studyInterventionUri, selectedIntervention) {
      return selectedIntervention.semanticInterventionUri === studyInterventionUri;
    }

    function addMissingOutcomesToStudies(studies, selectedOutcomes) {
      return studies.map(function(study) {
        var updatedStudy = _.cloneDeep(study);
        updatedStudy.missingOutcomes = findMissingOutcomes(updatedStudy, selectedOutcomes);
        return updatedStudy;
      });
    }

    function findMissingOutcomes(study, selectedOutcomes) {
      return _.filter(selectedOutcomes, function(selectedOutcome) {
        return noArmMatchingOutcome(selectedOutcome, study);
      });
    }

    function noArmMatchingOutcome(selectedOutcome, study) {
      return !_.some(study.arms, function(arm) {
        return _.some(arm.measurements[study.defaultMeasurementMoment], function(measurement) {
          return measurement.variableConceptUri === selectedOutcome.outcome.semanticOutcomeUri;
        });
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
    function recalculateGroup(studies) {
      return _.map(studies, function(study) {
        var modifiedStudy = {};
        if (isValidStudyOption(study)) {
          modifiedStudy.group = 0;
          modifiedStudy.groupLabel = 'Compatible studies';
        } else {
          modifiedStudy.group = 1;
          modifiedStudy.groupLabel = 'Incompatible Studies';
        }
        return _.merge({}, study, modifiedStudy);
      });
    }

    function isValidStudyOption(study) {
      var noMissingOutcomes = study.missingOutcomes ? study.missingOutcomes.length === 0 : true;
      var noMissingInterventions = study.missingInterventions ? study.missingInterventions.length === 0 : true;
      var noMixedTreatmentArm = !study.hasMatchedMixedTreatmentArm;
      return noMissingOutcomes && noMissingInterventions && noMixedTreatmentArm;
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

    return {
      addMissingInterventionsToStudies: addMissingInterventionsToStudies,
      addOverlappingInterventionsToStudies: addOverlappingInterventionsToStudies,
      addHasMatchedMixedTreatmentArm: addHasMatchedMixedTreatmentArm,
      addMissingOutcomesToStudies: addMissingOutcomesToStudies,
      recalculateGroup: recalculateGroup,
      findMissingOutcomes: findMissingOutcomes
    };
  };
  return dependencies.concat(SingleStudyBenefitRiskService);
});
