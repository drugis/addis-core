'use strict';
define([], function() {
  var dependencies = [];
  var MetaBenefitRiskAnalysisService = function() {

    function buildOutcomesWithAnalyses(analysis, networkMetaAnalyses, models, outcome) {
      var outcomeAnalyses = networkMetaAnalyses.filter(function(nma) {
        return nma.outcome.id === outcome.id;
      });

      // set the radioBtn state based on the stored inclusions
      var selectedAnalysis;
      var selectedModel;
      outcomeAnalyses.forEach(function(outcomeAnalysis) {
        var isInclusionSet = false;

        analysis.mbrOutcomeInclusions.forEach(function(outcomeInclusion) {
          if (outcomeInclusion.outcomeId === outcomeAnalysis.outcome.id &&
            outcomeInclusion.networkMetaAnalysisId === outcomeAnalysis.id) {
            selectedAnalysis = outcomeAnalysis;
            isInclusionSet = true;

            selectedModel = models.find(function(model) {
              return model.id === outcomeInclusion.modelId;
            });
          }
        });

        if (!isInclusionSet && outcomeAnalyses.length > 0 && outcome.isIncluded) {
          selectedAnalysis = outcomeAnalyses[0];
        }

      });
      return {
        outcome: outcome,
        networkMetaAnalyses: outcomeAnalyses,
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
        return owa.outcome.isIncluded ? ++count : count;
      }, 0);
    }

    function isModelWithMissingAlternatives(outcomesWithAnalyses) {
      return outcomesWithAnalyses.find(function(owa) {
        return owa.outcome.isIncluded && owa.selectedModel.missingAlternatives.length;
      });
    }

    function isModelWithoutResults(outcomesWithAnalyses) {
      return outcomesWithAnalyses.find(function(owa) {
        return owa.outcome.isIncluded && !owa.selectedModel.hasResult;
      });
    }

    function findMissingAlternatives(interventionInclusions, owa) {
      return interventionInclusions.filter(function(alternative) {
        var modelType = owa.selectedModel.modelType;
        if (modelType.type === 'pairwise') {
          return alternative.id !== modelType.details.from.id &&
            alternative.id !== modelType.details.to.id;
        } else {
          return !owa.selectedAnalysis.interventionInclusions.find(function(includedIntervention) {
            return alternative.id === includedIntervention.interventionId;
          });
        }
      });
    }

    function addScales(owas, interventionInclusions , scaleResults) {
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
      buildOutcomesWithAnalyses: buildOutcomesWithAnalyses,
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
