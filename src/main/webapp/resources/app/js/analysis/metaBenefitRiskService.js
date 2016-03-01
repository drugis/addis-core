'use strict';
define([], function() {
    var dependencies = [];
    var MetaBenefitRiskAnalysisService = function() {

      function buildOutcomesWithAnalyses(outcome, analysis, networkMetaAnalyses) {
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

      return {
        buildOutcomesWithAnalyses: buildOutcomesWithAnalyses
      };
    };

    return dependencies.concat(MetaBenefitRiskAnalysisService);
  });
