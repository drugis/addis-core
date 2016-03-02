'use strict';
define([], function() {
    var dependencies = [];
    var MetaBenefitRiskAnalysisService = function() {

      function buildOutcomesWithAnalyses(analysis, networkMetaAnalyses, models, outcome) {
        var outcomeAnalyses = networkMetaAnalyses.filter(function(nma) {
          return nma.outcome.id === outcome.id;
        });

        // set the radioBtn state based on the stored inclusions
        var selectedAnalysisId;
        var selectedModel;
        outcomeAnalyses.forEach(function(outcomeAnalysis) {
          var isInclusionSet = false;

          analysis.mbrOutcomeInclusions.forEach(function(outcomeInclusion) {
            if (outcomeInclusion.outcomeId === outcomeAnalysis.outcome.id &&
              outcomeInclusion.networkMetaAnalysisId === outcomeAnalysis.id) {
              selectedAnalysisId = outcomeAnalysis.id;
              isInclusionSet = true;

              selectedModel = models.find(function(model){
                return model.id === outcomeInclusion.modelId;
              });
            }
          });

          if (!isInclusionSet && outcomeAnalyses.length > 0 && outcome.isIncluded) {
            selectedAnalysisId = outcomeAnalyses[0].id;
          }

        });
        return {
          outcome: outcome,
          networkMetaAnalyses: outcomeAnalyses,
          selectedAnalysisId: selectedAnalysisId,
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
        if(a.models.length > 0) {
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

      return {
        addModelsGroup: addModelsGroup,
        compareAnalysesByModels: compareAnalysesByModels,
        buildOutcomesWithAnalyses: buildOutcomesWithAnalyses,
        joinModelsWithAnalysis: joinModelsWithAnalysis
      };
    };

    return dependencies.concat(MetaBenefitRiskAnalysisService);
  });
