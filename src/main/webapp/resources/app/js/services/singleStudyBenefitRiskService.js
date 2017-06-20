'use strict';
define(['lodash'], function(_) {
  var dependencies = ['ProblemResource', 'ScenarioResource', 'SubProblemResource'];
  var SingleStudyBenefitRiskService = function(ProblemResource, ScenarioResource, SubProblemResource) {

    var verifyCell = function(performanceEntry, outcome, intervention) {
      var result = performanceEntry.criterionUri === outcome.semanticOutcomeUri && performanceEntry.alternativeUri === intervention.semanticInterventionUri;
      return result;
    };

    var findPerformanceEntry = function(performanceTable, outcome, intervention) {
      var result = _.find(performanceTable, function(performanceEntry) {
        return verifyCell(performanceEntry, outcome, intervention);
      });
      return result;
    };

    var checkAllInterventionsForOutcome = function(performanceTable, outcome, selectedInterventions) {
      var result = _.every(selectedInterventions, function(intervention) {
        return findPerformanceEntry(performanceTable, outcome, intervention);
      });
      return result;
    };

    var validateProblem = function(analysis, problem) {
      var selectedOutcomes = analysis.selectedOutcomes;
      var selectedInterventions = analysis.selectedInterventions;
      return _.every(selectedOutcomes, function(outcome) {
        return checkAllInterventionsForOutcome(problem.performanceTable, outcome, selectedInterventions);
      });
    };

    var getProblem = function(projectId, analysisId) {
      return ProblemResource.get({
        projectId: projectId,
        analysisId: analysisId
      }).$promise;
    };

    var concatWithNoDuplicates = function(source, target, comparatorFunction) {
      var complement = findMissing(source, target, comparatorFunction);
      return complement.concat(target);
    };

    function findMissing(searchList, optionList, comparatorFunction) {
      return _.filter(searchList, function(searchItem) {
        return !_.find(optionList, function(option) {
          return comparatorFunction(option, searchItem);
        });
      });
    }

    function isValidStudyOption(study) {
      var noMissingOutcomes = study.missingOutcomes ? study.missingOutcomes.length === 0 : true;
      var noMissingInterventions = study.missingInterventions ? study.missingInterventions.length === 0 : true;
      var noMixedTreatmentArm = !study.hasMatchedMixedTreatmentArm;
      return noMissingOutcomes && noMissingInterventions && noMixedTreatmentArm;
    }

    function isSameIntervention(studyInterventionUri, selectedIntervention) {
      return selectedIntervention.semanticInterventionUri === studyInterventionUri;
    }

    function noArmMatchingOutcome(selectedOutcome, study) {
      return !_.find(study.trialDataArms, function(arm) {
        return _.find(arm.measurements[study.defaultMeasurementMoment], function(measurement) {
          return measurement.variableConceptUri === selectedOutcome.outcome.semanticOutcomeUri;
        });
      });
    }

    function findMissingOutcomes(selectedOutcomes, study) {
      return _.filter(selectedOutcomes, function(selectedOutcome) {
        return noArmMatchingOutcome(selectedOutcome, study);
      });
    }

    function noArmMatchingIntervention(intervention, trialDataArms) {
      return !_.find(trialDataArms, function(arm) {
        return arm.matchedProjectInterventionIds.indexOf(intervention.id) > -1;
      });
    }

    function findMissingInterventions(selectedInterventions, trialDataArms) {
      return _.filter(selectedInterventions, function(selectedIntervention) {
        return noArmMatchingIntervention(selectedIntervention, trialDataArms);
      });
    }

    var addMissingOutcomesToStudies = function(studies, selectedOutcomes) {
      return studies.map(function(study) {
        study.missingOutcomes = findMissingOutcomes(selectedOutcomes, study);
        return study;
      });
    };

    function addMissingInterventionsToStudies(studies, selectedInterventions) {
      return studies.map(function(study) {
        study.missingInterventions = findMissingInterventions(selectedInterventions, study.trialDataArms);
        return study;
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

    function findOverlappingIntervention(selectedInterventions, study) {
      return _.reduce(study.trialDataArms, function(accum, arm) {
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

    function getDefaultSubProblem(params) {
      return SubProblemResource
        .query(_.omit(params, 'id'))
        .$promise
        .then(function(scenarios) {
          return scenarios[0];
        });
    }

    var getDefaultScenario = function(params) {
      return ScenarioResource
        .query(_.omit(params, 'id'))
        .$promise
        .then(function(scenarios) {
          return scenarios[0];
        });
    };

    function getDefaultScenarioIds(params) {
      return getDefaultSubProblem(params).then(function(subProblem) {
        var newParams = _.extend({
          problemId: subProblem.id
        }, params);
        return getDefaultScenario(newParams).then(function(scenario) {
          return {
            scenario: scenario.id,
            problem: subProblem.id
          };
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
      getProblem: getProblem,
      validateProblem: validateProblem,
      getDefaultScenario: getDefaultScenario,
      concatWithNoDuplicates: concatWithNoDuplicates,
      addMissingOutcomesToStudies: addMissingOutcomesToStudies,
      addMissingInterventionsToStudies: addMissingInterventionsToStudies,
      addOverlappingInterventionsToStudies: addOverlappingInterventionsToStudies,
      addHasMatchedMixedTreatmentArm: addHasMatchedMixedTreatmentArm,
      isValidStudyOption: isValidStudyOption,
      recalculateGroup: recalculateGroup,
      getDefaultScenarioIds: getDefaultScenarioIds
    };
  };
  return dependencies.concat(SingleStudyBenefitRiskService);
});
