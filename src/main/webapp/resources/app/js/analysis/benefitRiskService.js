'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$state',
    'ProblemResource',
    'AnalysisResource',
    'SubProblemResource',
    'ScenarioResource',
    'WorkspaceService',
    'BenefitRiskErrorService',
    'DEFAULT_VIEW'
  ];
  var BenefitRiskAnalysisService = function(
    $state,
    ProblemResource,
    AnalysisResource,
    SubProblemResource,
    ScenarioResource,
    WorkspaceService,
    BenefitRiskErrorService,
    DEFAULT_VIEW
  ) {

    function buildOutcomesWithAnalyses(analysis, studies, networkMetaAnalyses, models, outcomes) {
      var filtered = filterArchivedAndAddModels(networkMetaAnalyses, models);
      var outcomesWithAnalyses = _(outcomes)
        .map(_.partial(buildOutcomeWithAnalyses, analysis, filtered))
        .map(function(outcomeWithAnalysis) {
          outcomeWithAnalysis.networkMetaAnalyses = outcomeWithAnalysis.networkMetaAnalyses.sort(
            compareAnalysesByModels
          );
          return outcomeWithAnalysis;
        })
        .value();
      return addStudiesToOutcomes(outcomesWithAnalyses, analysis.benefitRiskStudyOutcomeInclusions, studies);
    }

    function filterArchivedAndAddModels(networkMetaAnalyses, models) {
      var nonArchived = filterArchived(networkMetaAnalyses);
      return addModels(nonArchived, models);
    }

    function filterArchived(networkMetaAnalyses) {
      return _.reject(networkMetaAnalyses, 'archived');
    }

    function addModels(networkMetaAnalyses, models) {
      return _(networkMetaAnalyses)
        .map(_.partial(joinModelsWithAnalysis, models))
        .map(addModelsGroup)
        .value();
    }

    function buildOutcomes(analysis, outcomes, networkMetaAnalyses, studies) {
      var outcomesById = _.keyBy(outcomes, 'id');
      var outcomesWithAnalysis = getIncludedNmaOutcomes(analysis, networkMetaAnalyses, outcomesById);
      var includedStudyOutcomes = getIncludedStudyOutcomes(analysis.benefitRiskStudyOutcomeInclusions, outcomesById);
      var studyOutcomes = addStudiesToOutcomes(includedStudyOutcomes, analysis.benefitRiskStudyOutcomeInclusions, studies);
      return outcomesWithAnalysis.concat(studyOutcomes);
    }

    function addStudiesToOutcomes(outcomes, studyInclusions, studies) {
      return _.map(outcomes, function(outcome) {
        var outcomeCopy = _.cloneDeep(outcome);
        var inclusion = _.find(studyInclusions, ['outcomeId', outcomeCopy.outcome.id]);
        if (inclusion) {
          outcomeCopy.dataType = 'single-study';
          if (inclusion.studyGraphUri) {
            var study = _.find(studies, ['studyUri', inclusion.studyGraphUri]);
            outcomeCopy.selectedStudy = study;
            outcomeCopy.isContrastOutcome = _.some(study.arms, function(arm) {
              return _.some(arm.measurements[study.defaultMeasurementMoment], function(measurement) {
                return measurement.referenceArm;
              });
            });
          } else {
            outcomeCopy.selectedStudy = {};
          }
        }
        return outcomeCopy;
      });
    }


    function getIncludedNmaOutcomes(analysis, nmas, outcomes) {
      return _.map(analysis.benefitRiskNMAOutcomeInclusions, function(inclusion) {
        return buildOutcomeWithAnalyses(analysis, nmas, outcomes[inclusion.outcomeId]);
      });
    }

    function getIncludedStudyOutcomes(inclusions, outcomes) {
      return _.map(inclusions, function(inclusion) {
        var outcome = {
          outcome: outcomes[inclusion.outcomeId]
        };
        if (inclusion.baseline) {
          outcome.baseline = inclusion.baseline;
        }
        return outcome;
      });
    }

    function buildOutcomeWithAnalyses(analysis, networkMetaAnalyses, outcome) {
      var nmasForOutcome = networkMetaAnalyses.filter(function(nma) {
        return nma.outcome.id === outcome.id;
      });

      var benefitRiskNMAOutcomeInclusion = _.find(analysis.benefitRiskNMAOutcomeInclusions, ['outcomeId', outcome.id]);
      if (!benefitRiskNMAOutcomeInclusion) {
        return {
          outcome: outcome,
          networkMetaAnalyses: nmasForOutcome
        };
      }
      var selectedAnalysis = _.find(nmasForOutcome, function(nma) {
        return outcome.id === nma.outcome.id &&
          benefitRiskNMAOutcomeInclusion.networkMetaAnalysisId === nma.id;
      }) || nmasForOutcome[0];
      var selectedModel = !selectedAnalysis ? undefined : _.find(selectedAnalysis.models, ['id', benefitRiskNMAOutcomeInclusion.modelId]);

      var outcomeWithAnalysis = {
        outcome: outcome,
        networkMetaAnalyses: nmasForOutcome,
        selectedAnalysis: selectedAnalysis,
        selectedModel: selectedModel,
        dataType: 'network',
      };
      if (benefitRiskNMAOutcomeInclusion.baseline) {
        outcomeWithAnalysis.baseline = benefitRiskNMAOutcomeInclusion.baseline;
      }
      return outcomeWithAnalysis;
    }

    function joinModelsWithAnalysis(models, networkMetaAnalysis) {
      networkMetaAnalysis.models = models.filter(function(model) {
        return model.analysisId === networkMetaAnalysis.id;
      });
      return networkMetaAnalysis;
    }

    function compareAnalysesByModels(a, b) {
      if (a.models.length > 0) {
        if (!b.models.length) {
          return -1;
        } else {
          return 0;
        }
      } else {
        if (b.models.length > 0) {
          return 1;
        }
      }
      return 0;
    }

    function addModelsGroup(analysis) {
      analysis.models = analysis.models.map(function(model) {
        model.group = analysis.primaryModel === model.id ? 'Primary model' : 'Other models';
        return model;
      });
      return analysis;
    }


    function findMissingAlternatives(interventionInclusions, outcomeWithAnalysis) {
      return interventionInclusions.filter(function(alternative) {
        var modelType = outcomeWithAnalysis.selectedModel.modelType;
        if (modelType.type === 'pairwise') {
          return alternative.id !== modelType.details.from.id &&
            alternative.id !== modelType.details.to.id;
        } else {
          return !_.find(outcomeWithAnalysis.selectedAnalysis.interventionInclusions, function(includedIntervention) {
            return alternative.id === includedIntervention.interventionId;
          });
        }
      });
    }

    function addScales(outcomes, alternatives, criteria, scaleResults) {
      var includedAlternatives = getIncludedAlternatives(alternatives);
      return outcomes.map(function(outcome) {
        outcome.scales = includedAlternatives.reduce(function(accum, includedAlternative) {
          var outcomeUri = outcome.outcome.semanticOutcomeUri;
          if (!criteria[outcomeUri]) { return accum; }
          var dataSourceId = criteria[outcomeUri].dataSources[0].id;
          if (scaleResults[dataSourceId]) {
            accum[includedAlternative.id] = scaleResults[dataSourceId][includedAlternative.id];
          }
          return accum;
        }, {});
        return outcome;
      });
    }

    function getIncludedAlternatives(alternatives) {
      return _.filter(alternatives, function(alternative) {
        return alternative.isIncluded;
      });
    }

    function findOverlappingInterventions(studies) {
      var overlappingInterventionsList = _.reduce(studies, function(accum, study) {
        return accum.concat(study.overlappingInterventions);
      }, []);
      return _.uniqBy(overlappingInterventionsList, 'id');
    }

    function addBaseline(analysis, models, alternatives) {
      var newAnalysis = angular.copy(analysis);
      newAnalysis.benefitRiskNMAOutcomeInclusions = _.map(
        newAnalysis.benefitRiskNMAOutcomeInclusions,
        _.partial(addBaselineIfNeeded, newAnalysis, models, alternatives)
      );
      return newAnalysis;
    }

    function addBaselineIfNeeded(newAnalysis, models, alternatives, outcome) {
      if (outcome.baseline) { return outcome; }
      var baselineModel = findModelForBaseline(models, outcome);
      if (baselineModel && baselineModel.baseline &&
        hasIncludedIntervention(newAnalysis.interventionInclusions, alternatives, baselineModel)
      ) {
        outcome.baseline = baselineModel.baseline.baseline;
      }
      return outcome;
    }

    function findModelForBaseline(models, outcome) {
      return _.find(models, function(model) {
        return model.id === outcome.modelId;
      });
    }

    function hasIncludedIntervention(interventionInclusions, alternatives, baselineModel) {
      return _.find(interventionInclusions, function(interventionInclusion) {
        return _.find(alternatives, function(alternative) {
          return interventionInclusion.interventionId === alternative.id;
        }).name.localeCompare(baselineModel.baseline.baseline.name) === 0;
      });
    }

    function analysisToSaveCommand(analysis, problem) {
      var analysisToSave = angular.copy(analysis);
      return {
        id: analysis.id,
        projectId: analysis.projectId,
        analysis: analysisToSave,
        scenarioState: JSON.stringify(problem, null, 2)
      };
    }

    function finalizeAndGoToDefaultScenario(analysis) {
      return ProblemResource.get($state.params).$promise.then(function(problem) {
        var saveCommand = analysisToSaveCommand(analysis, {
          problem: WorkspaceService.reduceProblem(problem)
        });
        AnalysisResource.save(saveCommand, function() {
          goToDefaultScenario();
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

    function goToDefaultScenario() {
      var params = $state.params;
      return getSubProblems(params)
        .then(_.partial(getExtendedParams, params))
        .then(goToScenario);
    }

    function getSubProblems(params) {
      return SubProblemResource.query(params).$promise;
    }

    function getExtendedParams(params, subProblems) {
      var subProblem = subProblems[0];
      params = _.extend({}, params, {
        problemId: subProblem.id
      });
      return ScenarioResource.query(params).$promise.then(function(scenarios) {
        return _.extend({}, params, {
          id: scenarios[0].id
        });
      });
    }

    function goToScenario(params) {
      $state.go(DEFAULT_VIEW, params);
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

    function getOutcomesWithInclusions(outcomes, analysis) {
      var inclusions = analysis.benefitRiskNMAOutcomeInclusions.concat(analysis.benefitRiskStudyOutcomeInclusions);
      return _.map(outcomes, function(outcome) {
        outcome.isIncluded = _.some(inclusions, function(outcomeInclusion) {
          return outcomeInclusion.outcomeId === outcome.id;
        });
        return outcome;
      });
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

    function prepareEffectsTable(outcomes) {
      var outcomeIds = _(outcomes).filter('isIncluded').map('id').value();
      return AnalysisResource.query({
        projectId: $state.params.projectId,
        outcomeIds: outcomeIds
      }).$promise;
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

    return {
      addModelsGroup: addModelsGroup,//public for test
      joinModelsWithAnalysis: joinModelsWithAnalysis,//public for test
      compareAnalysesByModels: compareAnalysesByModels,//public for test
      addStudiesToOutcomes: addStudiesToOutcomes,//public for test
      buildOutcomeWithAnalyses: buildOutcomeWithAnalyses,//public for test
      buildOutcomes: buildOutcomes,
      findMissingAlternatives: findMissingAlternatives,
      addScales: addScales,
      findOverlappingInterventions: findOverlappingInterventions,
      addBaseline: addBaseline,
      analysisToSaveCommand: analysisToSaveCommand,
      analysisUpdateCommand: analysisUpdateCommand,
      finalizeAndGoToDefaultScenario: finalizeAndGoToDefaultScenario,
      goToDefaultScenario: goToDefaultScenario,
      isContrastStudySelected: isContrastStudySelected,
      getOutcomesWithInclusions: getOutcomesWithInclusions,
      getStep1Errors: getStep1Errors,
      filterArchivedAndAddModels: filterArchivedAndAddModels,
      addModels: addModels,
      buildOutcomesWithAnalyses: buildOutcomesWithAnalyses,
      prepareEffectsTable: prepareEffectsTable,
      getStudyOutcomeInclusions: getStudyOutcomeInclusions,
      getNMAOutcomeInclusions: getNMAOutcomeInclusions
    };
  };

  return dependencies.concat(BenefitRiskAnalysisService);
});
