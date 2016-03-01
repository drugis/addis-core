'use strict';
define([], function() {
    var dependencies = [];
    var MetaBenefitRiskAnalysisService = function() {

      function buildOutcomesWithAnalyses(analysis, networkMetaAnalyses, outcome) {
        var outcomeAnalyses = networkMetaAnalyses.filter(function(nma) {
          return nma.outcome.id === outcome.id;
        });

        // set the radioBtn state based on the stored inclusions
        var selectedAnalysisId;
        outcomeAnalyses.forEach(function(outcomeAnalysis) {
          var isInclusionSet = false;

          analysis.mbrOutcomeInclusions.forEach(function(outcomeInclusion) {
            if (outcomeInclusion.outcomeId === outcomeAnalysis.outcome.id &&
              outcomeInclusion.networkMetaAnalysisId === outcomeAnalysis.id) {
              selectedAnalysisId = outcomeAnalysis.id;
              isInclusionSet = true;
            }
          });

          if (!isInclusionSet && outcomeAnalyses.length > 0 && outcome.isIncluded) {
            selectedAnalysisId = outcomeAnalyses[0].id;
          }

        });
        return {
          outcome: outcome,
          networkMetaAnalyses: outcomeAnalyses,
          selectedAnalysisId: selectedAnalysisId
        };
      }

      function joinModelsWithAnalysis(models, networkMetaAnalysis) {
        networkMetaAnalysis.models = models.filter(function(model) {
          return model.analysisId === networkMetaAnalysis.id;
        });
        return networkMetaAnalysis;
      }

      return {
        buildOutcomesWithAnalyses: buildOutcomesWithAnalyses,
        joinModelsWithAnalysis: joinModelsWithAnalysis
      };
    };

    return dependencies.concat(MetaBenefitRiskAnalysisService);
  });
