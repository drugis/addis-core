'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var BenefitRiskAnalysisService = function() {

    function isMissingDataType(outcomesWithAnalyses) {
      return _(outcomesWithAnalyses).filter('outcome.isIncluded').reject('dataType').value().length;
    }

    function isMissingAnalysis(outcomesWithAnalyses) {
      return _.find(outcomesWithAnalyses, function(outcomeWithAnalyses) {
        return outcomeWithAnalyses.dataType === 'network' && !outcomeWithAnalyses.selectedAnalysis;
      });
    }

    function addStudiesToOutcomes(outcomesWithAnalyses, studyInclusions, studies) {
      return _.map(outcomesWithAnalyses, function(outcomeWithAnalyses) {
        var outcomeWithAnalysesCopy = _.cloneDeep(outcomeWithAnalyses);
        var inclusion = _.find(studyInclusions, ['outcomeId', outcomeWithAnalysesCopy.outcome.id]);
        if (inclusion) {
          outcomeWithAnalysesCopy.dataType = 'single-study';
          if (inclusion.studyGraphUri) {
            outcomeWithAnalysesCopy.selectedStudy = _.find(studies, ['studyUri', inclusion.studyGraphUri]);
          } else {
            outcomeWithAnalysesCopy.selectedStudy = {};
          }
        }
        return outcomeWithAnalysesCopy;
      });
    }

    function buildOutcomesWithAnalyses(analysis, outcomes, networkMetaAnalyses) {
      var outcomesById = _.keyBy(outcomes, 'id');
      var outcomesWithAnalysis = _.map(analysis.benefitRiskNMAOutcomeInclusions, function(benefitRiskNMAOutcomeInclusion) {
        return buildOutcomeWithAnalyses(analysis, networkMetaAnalyses, outcomesById[benefitRiskNMAOutcomeInclusion.outcomeId]);
      });
      var outcomesWithStudy = _.map(analysis.benefitRiskStudyOutcomeInclusions, function(benefitRiskStudyOutcomeInclusion) {
        return buildOutcomeWithStudy(analysis, benefitRiskStudyOutcomeInclusion.studyGraphUri, outcomesById[benefitRiskStudyOutcomeInclusion.outcomeId]);
      });
      return outcomesWithAnalysis.concat(outcomesWithStudy);
    }

    function buildOutcomeWithStudy(analysis, studyGraphUri, outcome) {
      return {
        outcome: outcome
      };
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

      var owa = {
        outcome: outcome,
        networkMetaAnalyses: nmasForOutcome,
        selectedAnalysis: selectedAnalysis,
        selectedModel: selectedModel,
        dataType: 'network',
      };
      if (benefitRiskNMAOutcomeInclusion.baseline) {
        owa.baselineDistribution = benefitRiskNMAOutcomeInclusion.baseline;
      }
      return owa;
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

    function numberOfSelectedOutcomes(outcomeInclusions) {
      var goodOutcomes = _.filter(outcomeInclusions, function(inclusion) {
        if (!inclusion.outcome.isIncluded) {
          return false;
        } else {
          if (inclusion.selectedAnalysis) {
            return !inclusion.selectedAnalysis.archived && inclusion.selectedModel && !inclusion.selectedModel.archived;
          } else {
            return inclusion.selectedStudy;
          }
        }
      });
      return goodOutcomes.length;
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

    function addScales(outcomesWithAnalyses, interventionInclusions, criteria, scaleResults) {
      return outcomesWithAnalyses.map(function(outcomeWithAnalyses) {
        outcomeWithAnalyses.scales = interventionInclusions.reduce(function(accum, includedAlternative) {
          var outcomeUri = outcomeWithAnalyses.outcome.semanticOutcomeUri;
          if(!criteria[outcomeUri]) { return accum; }
          var dataSourceId = criteria[outcomeUri].dataSources[0].id;
          if (scaleResults[dataSourceId]) {
            accum[includedAlternative.id] = scaleResults[dataSourceId][includedAlternative.id];
          }
          return accum;
        }, {});
        return outcomeWithAnalyses;
      });
    }

    function isInvalidStudySelected(outcomeInclusions) {
      var invalidStudy = _.chain(outcomeInclusions)
        .filter(['dataType', 'single-study'])
        .find(function(inclusion) {
          return (
            (inclusion.selectedStudy && inclusion.selectedStudy.missingInterventions && inclusion.selectedStudy.missingInterventions.length > 0) ||
            (inclusion.selectedStudy && inclusion.selectedStudy.missingOutcomes && inclusion.selectedStudy.missingOutcomes.length > 0)
          );
        }).value();
      return invalidStudy;
    }

    function hasMissingStudy(outcomeInclusions) {
      return _.find(outcomeInclusions, function(inclusion) {
        return inclusion.dataType === 'single-study' && _.isEmpty(inclusion.selectedStudy);
      });
    }

    function findOverlappingInterventions(studies) {
      var overlappingInterventionsList = _.reduce(studies, function(accum, study) {
        return accum.concat(study.overlappingInterventions);
      }, []);
      return _.uniqBy(overlappingInterventionsList, 'id');
    }

    function findOverlappingOutcomes(outcomeInclusions) {
      return _.chain(outcomeInclusions)
        .map('outcome')
        .filter('isIncluded')
        .groupBy('semanticOutcomeUri')
        .filter(function(outcomeByUri) {
          return outcomeByUri.length > 1;
        })
        .value();
    }

    function addModelBaseline(analysis, models, alternatives) {
      _.forEach(analysis.benefitRiskNMAOutcomeInclusions, function(benefitRiskNMAOutcomeInclusion) {
        if (!benefitRiskNMAOutcomeInclusion.baseline) {
          // there is no baseline set yet, check if you can use the modelBaseline
          var baselineModel = _.find(models, function(model) {
            return model.id === benefitRiskNMAOutcomeInclusion.modelId;
          });
          if (baselineModel && baselineModel.baseline) {
            // there is a model with a baseline, yay
            if (_.find(analysis.interventionInclusions, function(interventionInclusion) {
                //there is an intervention with the right name!
                return _.find(alternatives, function(alternative) {
                  return interventionInclusion.interventionId === alternative.id;
                }).name.localeCompare(baselineModel.baseline.baseline.name) === 0;
              })) {
              benefitRiskNMAOutcomeInclusion.baseline = baselineModel.baseline.baseline;
            }
          }
        }
      });
      return analysis;
    }

    return {
      isMissingDataType: isMissingDataType,
      isMissingAnalysis: isMissingAnalysis,
      addModelsGroup: addModelsGroup,
      addStudiesToOutcomes: addStudiesToOutcomes,
      compareAnalysesByModels: compareAnalysesByModels,
      buildOutcomeWithAnalyses: buildOutcomeWithAnalyses,
      buildOutcomesWithAnalyses: buildOutcomesWithAnalyses,
      joinModelsWithAnalysis: joinModelsWithAnalysis,
      numberOfSelectedOutcomes: numberOfSelectedOutcomes,
      isModelWithMissingAlternatives: isModelWithMissingAlternatives,
      isModelWithoutResults: isModelWithoutResults,
      findMissingAlternatives: findMissingAlternatives,
      addScales: addScales,
      isInvalidStudySelected: isInvalidStudySelected,
      hasMissingStudy: hasMissingStudy,
      findOverlappingInterventions: findOverlappingInterventions,
      findOverlappingOutcomes: findOverlappingOutcomes,
      addModelBaseline: addModelBaseline
    };
  };

  return dependencies.concat(BenefitRiskAnalysisService);
});