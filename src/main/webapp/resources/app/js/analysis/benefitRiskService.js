'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$state',
    'ProblemResource',
    'AnalysisResource',
    'SubProblemResource',
    'ScenarioResource',
    'WorkspaceService',
    'DEFAULT_VIEW'
  ];
  var BenefitRiskService = function(
    $state,
    ProblemResource,
    AnalysisResource,
    SubProblemResource,
    ScenarioResource,
    WorkspaceService,
    DEFAULT_VIEW
  ) {
    function addModels(networkMetaAnalyses, models) {
      return _(networkMetaAnalyses)
        .map(_.partial(joinModelsWithAnalysis, models))
        .map(addModelsGroup)
        .value();
    }

    function addModelsGroup(analysis) {
      analysis.models = analysis.models.map(function(model) {
        model.group = analysis.primaryModel === model.id ? 'Primary model' : 'Other models';
        return model;
      });
      return analysis;
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
            outcomeCopy.isContrastOutcome = isContrastOutcome(study, outcome);
          } else {
            outcomeCopy.selectedStudy = {};
          }
        }
        return outcomeCopy;
      });
    }

    function isContrastOutcome(study, outcome) {
      return _.some(study.arms, function(arm) {
        return _.some(arm.measurements[study.defaultMeasurementMoment], function(measurement) {
          return measurement.referenceArm && measurement.variableConceptUri === outcome.outcome.semanticOutcomeUri;
        });
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
      var nmasForOutcome = _.filter(networkMetaAnalyses, function(nma) {
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

    function getOutcomesWithInclusions(outcomes, inclusions) {
      return _.map(outcomes, function(outcome) {
        outcome.isIncluded = isOutcomeIncluded(outcome, inclusions);
        return outcome;
      });
    }

    function getAlternativesWithInclusion(alternatives, inclusions) {
      return _.map(alternatives, function(alternative) {
        alternative.isIncluded = isAlternativeIncluded(alternative, inclusions);
        return alternative;
      });
    }

    function isAlternativeIncluded(alternative, inclusions) {
      return _.some(inclusions, ['interventionId', alternative.id]);
    }

    function isOutcomeIncluded(outcome, inclusions) {
      return _.some(inclusions, ['outcomeId', outcome.id]);
    }

    function getIncludedAlternatives(alternatives) {
      return _.filter(alternatives, 'isIncluded');
    }

    function hasMissingBaseline(outcomes) {
      return _.some(outcomes, function(outcome) {
        return outcome.dataType === 'network' && !outcome.baseline ||
          outcome.dataType === 'single-study' && outcome.isContrastOutcome && !outcome.baseline;
      });
    }

    return {
      addModelsGroup: addModelsGroup, //1
      analysisToSaveCommand: analysisToSaveCommand, // 2
      joinModelsWithAnalysis: joinModelsWithAnalysis,//public for test
      addStudiesToOutcomes: addStudiesToOutcomes, //1 
      buildOutcomeWithAnalyses: buildOutcomeWithAnalyses, //1
      goToDefaultScenario: goToDefaultScenario, // 0 1 2
      getOutcomesWithInclusions: getOutcomesWithInclusions, // 1 2
      buildOutcomes: buildOutcomes, // 0 1 2 
      finalizeAndGoToDefaultScenario: finalizeAndGoToDefaultScenario, // 1 2
      addModels: addModels, // 0 2
      getAlternativesWithInclusion: getAlternativesWithInclusion, // 0 
      getIncludedAlternatives: getIncludedAlternatives, // 0
      hasMissingBaseline: hasMissingBaseline,// 0 2

    };
  };

  return dependencies.concat(BenefitRiskService);
});
