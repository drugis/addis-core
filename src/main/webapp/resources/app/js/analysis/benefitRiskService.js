'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var BenefitRiskAnalysisService = function() {

    function buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses) {
      return outcomes
        .map(_.partial(buildOutcomeWithAnalyses, analysis, networkMetaAnalyses))
        .map(function(outcomeWithAnalysis) {
          outcomeWithAnalysis.networkMetaAnalyses = outcomeWithAnalysis.networkMetaAnalyses.sort(compareAnalysesByModels);
          return outcomeWithAnalysis;
        })
        .filter(function(outcomeWithAnalysis) {
          return outcomeWithAnalysis.outcome.isIncluded;
        })
        .map(function(outcomeWithAnalysis) {
          outcomeWithAnalysis.baselineDistribution = analysis.benefitRiskNMAOutcomeInclusions.find(function(inclusion) {
            return inclusion.outcomeId === outcomeWithAnalysis.outcome.id;
          }).baseline;
          return outcomeWithAnalysis;
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
      var selectedModel = !selectedAnalysis? undefined : _.find(selectedAnalysis.models, ['id', benefitRiskNMAOutcomeInclusion.modelId]);

      return {
        outcome: outcome,
        networkMetaAnalyses: nmasForOutcome,
        selectedAnalysis: selectedAnalysis,
        selectedModel: selectedModel
      };
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

    function numberOfSelectedOutcomes(outcomesWithAnalyses) {
      return outcomesWithAnalyses.reduce(function(count, owa) {
        return owa.outcome.isIncluded &&
          owa.selectedAnalysis && !owa.selectedAnalysis.archived &&
          owa.selectedModel && !owa.selectedModel.archived ? ++count : count;
      }, 0);
    }

    function isModelWithMissingAlternatives(outcomesWithAnalyses) {
      return _.find(outcomesWithAnalyses, function(owa) {
        return owa.outcome.isIncluded && owa.selectedModel && owa.selectedModel.missingAlternatives.length;
      });
    }

    function isModelWithoutResults(outcomesWithAnalyses) {
      return _.find(outcomesWithAnalyses, function(owa) {
        return owa.outcome.isIncluded && owa.selectedModel && owa.selectedModel.runStatus !== 'done';
      });
    }

    function findMissingAlternatives(interventionInclusions, owa) {
      return interventionInclusions.filter(function(alternative) {
        var modelType = owa.selectedModel.modelType;
        if (modelType.type === 'pairwise') {
          return alternative.id !== modelType.details.from.id &&
            alternative.id !== modelType.details.to.id;
        } else {
          return !_.find(owa.selectedAnalysis.interventionInclusions, function(includedIntervention) {
            return alternative.id === includedIntervention.interventionId;
          });
        }
      });
    }

    function addScales(owas, interventionInclusions, scaleResults) {
      return owas.map(function(owa) {
        owa.scales = interventionInclusions.reduce(function(accum, includedAlternative) {
          if (scaleResults[owa.outcome.name]) {
            accum[includedAlternative.name] = scaleResults[owa.outcome.name][includedAlternative.name];
          }
          return accum;
        }, {});
        return owa;
      });
    }

    return {
      addModelsGroup: addModelsGroup,
      compareAnalysesByModels: compareAnalysesByModels,
      buildOutcomeWithAnalyses: buildOutcomeWithAnalyses,
      buildOutcomesWithAnalyses: buildOutcomesWithAnalyses,
      joinModelsWithAnalysis: joinModelsWithAnalysis,
      numberOfSelectedOutcomes: numberOfSelectedOutcomes,
      isModelWithMissingAlternatives: isModelWithMissingAlternatives,
      isModelWithoutResults: isModelWithoutResults,
      findMissingAlternatives: findMissingAlternatives,
      addScales: addScales
    };
  };

  return dependencies.concat(BenefitRiskAnalysisService);
});
