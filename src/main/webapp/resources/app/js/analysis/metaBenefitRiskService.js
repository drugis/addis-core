'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var MetaBenefitRiskAnalysisService = function() {

    function buildOutcomeWithAnalyses(analysis, networkMetaAnalyses, models, outcome) {
      var nmasForOutcome = networkMetaAnalyses.filter(function(nma) {
        return nma.outcome.id === outcome.id;
      });

      var mbrOutcomeInclusion = _.find(analysis.mbrOutcomeInclusions, ['outcomeId', outcome.id]);

      var selectedAnalysis = _.find(nmasForOutcome, function(nma) {
        return mbrOutcomeInclusion.outcomeId === nma.outcome.id &&
          mbrOutcomeInclusion.networkMetaAnalysisId === nma.id;
      }) || nmasForOutcome[0];
      var selectedModel = _.find(models, ['id', mbrOutcomeInclusion.modelId]);

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

    function numberOfSelectedInterventions(alternatives) {
      return alternatives.reduce(function(count, alternative) {
        return alternative.isIncluded ? ++count : count;
      }, 0);
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
      joinModelsWithAnalysis: joinModelsWithAnalysis,
      numberOfSelectedInterventions: numberOfSelectedInterventions,
      numberOfSelectedOutcomes: numberOfSelectedOutcomes,
      isModelWithMissingAlternatives: isModelWithMissingAlternatives,
      isModelWithoutResults: isModelWithoutResults,
      findMissingAlternatives: findMissingAlternatives,
      addScales: addScales
    };
  };

  return dependencies.concat(MetaBenefitRiskAnalysisService);
});
