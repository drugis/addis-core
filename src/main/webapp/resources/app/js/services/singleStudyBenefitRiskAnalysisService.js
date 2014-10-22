'use strict';
define(['angular'], function() {
  var dependencies = ['$location', '$stateParams', '$q', 'ProblemResource', 'ScenarioResource'];
  var SingleStudyBenefitRiskAnalysisService = function($location, $stateParams, $q, ProblemResource, ScenarioResource) {

    var getDefaultScenario = function() {
      return ScenarioResource
        .query(_.omit($stateParams, 'id'))
        .$promise
        .then(function(scenarios) {
          return scenarios[0];
        });
    };

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

    var getProblem = function() {
      return ProblemResource.get($stateParams).$promise;
    };

    var concatWithNoDuplicates = function(source, target, comparatorFunction) {
      var complement = findMissing(source, target, comparatorFunction);
      return complement.concat(target);
    };

    var findMissing = function(searchList, optionList, comparatorFunction) {
      return _.filter(searchList, function(searchItem) {
        return !_.find(optionList, function(option) {
          return comparatorFunction(option, searchItem);
        });
      });
    };

    var isValidStudyOption = function(study) {
      var noMissingOutcomes = study.missingOutcomes ? study.missingOutcomes.length === 0 : true;
      var noMissingInterventions = study.missingInterventions ? study.missingInterventions.length === 0 : true;
      var noMixedTreatmentArm = !study.hasMatchedMixedTreatmentArm;
      return noMissingOutcomes && noMissingInterventions && noMixedTreatmentArm;
    };

    // Add a 'group' property for sorting alphabetically within groups while placing the 'valid' group on top of the options list
    var addGroup = function(study) {
      if (isValidStudyOption(study)) {
        study.group = 0;
        study.groupLabel = 'Analysable studies';
      } else {
        study.group = 1;
        study.groupLabel = 'Un-analysable Studies';
      }
    };

    function isSameOutcome(studyOutcomeUri, selectedOutcome) {
      var lastIndexOfSlash = studyOutcomeUri.lastIndexOf('/');
      var idPart = studyOutcomeUri.substring(lastIndexOfSlash + 1);
      return selectedOutcome.semanticOutcomeUri === idPart;
    }

    function isSameIntervention(studyInterventionUri, selectedIntervention) {
      var lastIndexOfSlash = studyInterventionUri.lastIndexOf('/');
      var idPart = studyInterventionUri.substring(lastIndexOfSlash + 1);
      return selectedIntervention.semanticInterventionUri === idPart;
    }

    var addMissingOutcomesToStudies = function(studies, selectedOutcomes) {
      return _.each(studies, function(study) {
        study.missingOutcomes = findMissing(selectedOutcomes, study.outcomeUids, isSameOutcome);
      });
    };

    var addMissingInterventionsToStudies = function(studies, selectedInterventions) {
      return _.each(studies, function(study) {
        study.missingInterventions = findMissing(selectedInterventions, study.interventionUids, isSameIntervention);
      });
    };

    var recalculateGroup = function(studies) {
      _.each(studies, function(study) {
        addGroup(study);
      });
    };

    function findMatchingIntervention(selectedInterventions, treatmentArm) {
      return _.find(selectedInterventions, function(selectedIntervention) {
        return _.find(treatmentArm.interventionUids, function(interventionUid) {
          return isSameIntervention(interventionUid, selectedIntervention);
        });
      });
    }

    var addHasMatchedMixedTreatmentArm = function(studies, selectedInterventions) {
      _.each(studies, function(study) {
        study.hasMatchedMixedTreatmentArm = _.some(study.treatmentArms, function(treatmentArm) {
          return treatmentArm.interventionUids.length > 1 && findMatchingIntervention(selectedInterventions, treatmentArm);
        });
      });
    };

    return {
      getProblem: getProblem,
      validateProblem: validateProblem,
      getDefaultScenario: getDefaultScenario,
      concatWithNoDuplicates: concatWithNoDuplicates,
      addMissingOutcomesToStudies: addMissingOutcomesToStudies,
      addMissingInterventionsToStudies: addMissingInterventionsToStudies,
      addHasMatchedMixedTreatmentArm: addHasMatchedMixedTreatmentArm,
      isValidStudyOption: isValidStudyOption,
      recalculateGroup: recalculateGroup
    };
  };
  return dependencies.concat(SingleStudyBenefitRiskAnalysisService);
});