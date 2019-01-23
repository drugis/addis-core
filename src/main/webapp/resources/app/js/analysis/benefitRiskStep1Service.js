'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    'BenefitRiskErrorService',
    'BenefitRiskService'
  ];
  var BenefitRiskStep1Service = function(
    BenefitRiskErrorService,
    BenefitRiskService
  ) {
    function getReferenceAlternativeName(outcome, alternatives) {
      var referenceArm = _.find(outcome.selectedStudy.arms, function(arm) {
        return arm.referenceArm === arm.uri;
      });
      var referenceAlternativeId = referenceArm.matchedProjectInterventionIds[0];
      var referenceAlternative = _.find(alternatives, function(alternative) {
        return alternative.id === referenceAlternativeId;
      });
      return referenceAlternative.name;
    }

    function updateOutcomeInclusion(inclusion, alternatives) {
      var newInclusion = angular.copy(inclusion);
      if (!newInclusion.outcome.isIncluded) {
        newInclusion.selectedAnalysis = undefined;
        newInclusion.selectedStudy = undefined;
        newInclusion.selectedModel = undefined;
        newInclusion.dataType = undefined;
      } else {
        if (newInclusion.dataType === 'network') {
          newInclusion.selectedStudy = undefined;
          newInclusion.selectedAnalysis = findSelectableAnalysis(newInclusion);
          newInclusion.selectedModel = getModelSelection(newInclusion.selectedAnalysis);
          if (newInclusion.selectedModel) {
            newInclusion.selectedModel = updateMissingAlternatives(newInclusion, alternatives);
          }
        } else if (newInclusion.dataType === 'single-study') {
          newInclusion.selectedAnalysis = undefined;
          newInclusion.selectedModel = undefined;
          newInclusion.selectedStudy = {};
        }
      }
      return newInclusion;
    }

    function getModelSelection(analysis) {
      if (analysis) {
        var primaryModel = _.find(analysis.models, function(model) {
          return model.id === analysis.primaryModel;
        });
        if (primaryModel) {
          return angular.copy(primaryModel);
        } else {
          return angular.copy(analysis.models[0]);
        }
      } else {
        return undefined;
      }
    }

    function findSelectableAnalysis(inclusion) {
      return _.find(inclusion.networkMetaAnalyses, 'models.length');
    }

    function updateMissingAlternatives(outcome, alternatives) {
      return _.merge({}, outcome.selectedModel, {
        missingAlternatives: findMissingAlternatives(alternatives, outcome),
        missingAlternativesNames: _.map(outcome.selectedModel.missingAlternatives, 'name')
      });
    }

    function findMissingAlternatives(interventionInclusions, outcome) {
      return interventionInclusions.filter(function(alternative) {
        var modelType = outcome.selectedModel.modelType;
        if (modelType.type === 'pairwise') {
          return alternative.id !== modelType.details.from.id &&
            alternative.id !== modelType.details.to.id;
        } else {
          return !_.find(outcome.selectedAnalysis.interventionInclusions, function(includedIntervention) {
            return alternative.id === includedIntervention.interventionId;
          });
        }
      });
    }

    function getStudyOutcomeInclusions(outcomes, analysisId) {
      return _(outcomes)
        .filter(function(outcome) {
          return outcome.outcome.isIncluded && outcome.dataType === 'single-study';
        })
        .map(function(outcome) {
          return {
            analysisId: analysisId,
            outcomeId: outcome.outcome.id,
            studyGraphUri: outcome.selectedStudy ? outcome.selectedStudy.studyUri : undefined
          };
        })
        .value();
    }

    function getNMAOutcomeInclusions(outcomes, analysisId) {
      return _(outcomes)
        .filter(function(outcome) {
          return outcome.outcome.isIncluded && outcome.dataType === 'network' && outcome.selectedAnalysis;
        })
        .map(function(outcome) {
          return {
            analysisId: analysisId,
            outcomeId: outcome.outcome.id,
            networkMetaAnalysisId: outcome.selectedAnalysis.id,
            modelId: outcome.selectedModel ? outcome.selectedModel.id : undefined
          };
        })
        .value();
    }

    function getStep1Errors(outcomesWithAnalyses) {
      var errors = [];
      if (BenefitRiskErrorService.isInvalidStudySelected(outcomesWithAnalyses)) {
        errors.push('An invalid study is selected');
      }
      if (BenefitRiskErrorService.numberOfSelectedOutcomes(outcomesWithAnalyses) < 2) {
        errors.push('At least two outcomes must be selected');
      }
      if (BenefitRiskErrorService.isMissingAnalysis(outcomesWithAnalyses)) {
        errors.push('An outcome with missing network model is selected');
      }
      if (BenefitRiskErrorService.isMissingDataType(outcomesWithAnalyses)) {
        errors.push('The data source type of an outcome has not been chosen');
      }
      if (BenefitRiskErrorService.isModelWithMissingAlternatives(outcomesWithAnalyses)) {
        errors.push('A model with missing alternatives is selected');
      }
      if (BenefitRiskErrorService.isModelWithoutResults(outcomesWithAnalyses)) {
        errors.push('A model that has not yet run is selected');
      }
      if (BenefitRiskErrorService.hasMissingStudy(outcomesWithAnalyses)) {
        errors.push('A study still needs to be selected');
      }
      if (BenefitRiskErrorService.findOverlappingOutcomes(outcomesWithAnalyses).length > 0) {
        errors.push('There are overlapping outcomes');
      }
      return errors;
    }

    function isContrastStudySelected(includedOutcomes, studies) {
      return _.some(includedOutcomes, function(includedStudyOutcome) {
        return _.some(studies, function(study) {
          return includedStudyOutcome.studyGraphUri === study.studyUri && _.some(study.arms, function(arm) {
            return arm.referenceArm;
          });
        });
      });
    }

    function analysisUpdateCommand(analysis, includedAlternatives) {
      var analysisToSave = angular.copy(analysis);
      analysisToSave.interventionInclusions = includedAlternatives.map(function(intervention) {
        return {
          interventionId: intervention.id,
          analysisId: analysisToSave.id
        };
      });
      return {
        id: analysis.id,
        projectId: analysis.projectId,
        analysis: analysisToSave
      };
    }

    function findOverlappingInterventions(studies) {
      var overlappingInterventionsList = _.reduce(studies, function(accum, study) {
        return accum.concat(study.overlappingInterventions);
      }, []);
      return _.uniqBy(overlappingInterventionsList, 'id');
    }

    function buildOutcomesWithAnalyses(analysis, studies, networkMetaAnalyses, models, outcomes) {
      var filteredNmas = _.reject(networkMetaAnalyses, 'archived');
      var nmasWithModels = BenefitRiskService.addModels(filteredNmas, models);
      var outcomesWithAnalyses = _.map(outcomes, function(outcome) {
        var outcomeWithAnalysis = BenefitRiskService.buildOutcomeWithAnalyses(analysis, nmasWithModels, outcome);
        outcomeWithAnalysis.networkMetaAnalyses = _.sortBy(outcomeWithAnalysis.networkMetaAnalyses, compareAnalysesByModels);
        return outcomeWithAnalysis;
      });
      return BenefitRiskService.addStudiesToOutcomes(outcomesWithAnalyses, analysis.benefitRiskStudyOutcomeInclusions, studies);
    }

    function compareAnalysesByModels(a, b) {
      if (a && a.models && a.models.length > 0) {
        if (!b || !b.models || !b.models.length) {
          return -1;
        } else {
          return 0;
        }
      } else {
        if (b && b.models && b.models.length > 0) {
          return 1;
        }
      }
      return 0;
    }

    return {
      analysisUpdateCommand: analysisUpdateCommand,
      compareAnalysesByModels: compareAnalysesByModels, // exposed for testing
      findMissingAlternatives: findMissingAlternatives, // exposed for testing
      findOverlappingInterventions: findOverlappingInterventions,
      getModelSelection: getModelSelection,
      getNMAOutcomeInclusions: getNMAOutcomeInclusions,
      getReferenceAlternativeName: getReferenceAlternativeName,
      getStep1Errors: getStep1Errors,
      getStudyOutcomeInclusions: getStudyOutcomeInclusions,
      isContrastStudySelected: isContrastStudySelected,
      updateMissingAlternatives: updateMissingAlternatives,
      updateOutcomeInclusion: updateOutcomeInclusion,
      buildOutcomesWithAnalyses: buildOutcomesWithAnalyses
    };
  };
  return dependencies.concat(BenefitRiskStep1Service);
});
