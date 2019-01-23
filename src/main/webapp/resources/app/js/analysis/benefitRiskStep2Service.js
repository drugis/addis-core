'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$stateParams',
    'BenefitRiskService',
    'AnalysisResource'
  ];
  var BenefitRiskStep2Service = function(
    $stateParams,
    BenefitRiskService,
    AnalysisResource
  ) {
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

    function addBaselineToInclusion(outcome, inclusions, baseline) {
      return _.map(
        inclusions, function(inclusion) {
          if (inclusion.outcomeId === outcome.outcome.id) {
            return _.extend(inclusion, { baseline: baseline });
          } else {
            return inclusion;
          }
        }
      );
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
    
    function filterArchivedAndAddModels(networkMetaAnalyses, models) {
      var nonArchived = filterArchived(networkMetaAnalyses);
      return BenefitRiskService.addModels(nonArchived, models);
    }

    function filterArchived(networkMetaAnalyses) {
      return _.reject(networkMetaAnalyses, 'archived');
    }

    function getMeasurementType(outcome) {
      var study = outcome.selectedStudy;
      var nonReferenceArm = _.find(study.arms, function(arm) {
        return arm.referenceArm !== arm.uri;
      });
      var measurement = nonReferenceArm.measurements[study.defaultMeasurementMoment][0];
      var measurementTypes = [
        'oddsRatio',
        'riskRatio',
        'meanDifference',
        'standardizedMeanDifference',
        'hazardRatio'
      ];
      return _.find(measurementTypes, function(propertyName) {
        return measurement[propertyName];
      });
    }

    function prepareEffectsTable(outcomes) {
      var outcomeIds = _(outcomes).filter('isIncluded').map('id').value();
      return AnalysisResource.query({
        projectId: $stateParams.projectId,
        outcomeIds: outcomeIds
      }).$promise;
    }

    return {
      addBaseline: addBaseline,
      addBaselineToInclusion: addBaselineToInclusion,
      addScales: addScales,
      filterArchivedAndAddModels: filterArchivedAndAddModels,
      getMeasurementType: getMeasurementType,
      prepareEffectsTable: prepareEffectsTable,
    };
  };

  return dependencies.concat(BenefitRiskStep2Service);
});
