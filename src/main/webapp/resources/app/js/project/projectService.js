'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var ProjectService = function() {

    function checkforDuplicateName(itemList, itemToCheck) {
      return _.find(itemList, function(item) {
        return itemToCheck.name === item.name && (
          itemToCheck.id === undefined || itemToCheck.id !== item.id); // name is only duplicate if item is not compared to self
      });
    }

    function buildCovariateUsage(analyses, covariates) {
      var matcherFunction = function(analysis, covariate) {
        return _.find(analysis.includedCovariates, ['covariateId', covariate.id]);
      };
      return buildDefinitionUsage(analyses, covariates, matcherFunction);
    }

    function buildInterventionUsage(analyses, interventions) {
      var matcherFunction = function(analysis, intervention) {
        return _.find(analysis.interventionInclusions, ['interventionId', intervention.id]);
      };
      var usageInAnalyses = buildDefinitionUsage(analyses, interventions, matcherFunction);
      var usageInComplexInterventions = _
        .chain(interventions)
        .filter(function(intervention) {
          return intervention.type !== 'class';
        })
        .reduce(function(accum, intervention) {
          accum[intervention.id] = _.chain(interventions)
            .filter(function(otherIntervention) {
              return _.includes(otherIntervention.interventionIds, intervention.id);
            })
            .map('name')
            .value();
          return accum;
        }, {})
        .value();
      return {
        inAnalyses: usageInAnalyses,
        inInterventions: usageInComplexInterventions
      };
    }

    function buildOutcomeUsage(analyses, outcomes) {
      var matcherFunction = function(analysis, outcome) {
        if (analysis.analysisType === 'Evidence synthesis') {
          return analysis.outcome && analysis.outcome.id === outcome.id;
        } else if (analysis.analysisType === 'Benefit-risk analysis based on a single study') {
          return _.find(analysis.selectedOutcomes, ['id', outcome.id]);
        } else if (analysis.analysisType === 'Benefit-risk analysis based on meta-analyses') {
          return _.find(analysis.mbrOutcomeInclusions, ['outcomeId', outcome.id]);
        }

      };
      return buildDefinitionUsage(analyses, outcomes, matcherFunction);
    }

    function buildDefinitionUsage(analyses, definitions, matcherFunction) {
      return _.reduce(definitions, function(accum, definition) {
        accum[definition.id] = _.map(_.filter(analyses, function(analysis) {
          return matcherFunction(analysis, definition);
        }), 'title');
        return accum;
      }, {});
    }

    function isMultiplierMissing(constraint) {
      return (constraint.lowerBound && !constraint.lowerBound.conversionMultiplier) ||
        (constraint.upperBound && !constraint.upperBound.conversionMultiplier);
    }

    function addMissingMultiplierInfo(interventions) {
      return _.map(interventions, function(intervention) {
        if (intervention.type === 'fixed') {
          intervention.hasMissingMultipliers = isMultiplierMissing(intervention.constraint);
        } else if (intervention.type === 'titrated' || intervention.type === 'both') {
          intervention.hasMissingMultipliers = isMultiplierMissing(intervention.minConstraint) ||
            isMultiplierMissing(intervention.maxConstraint);
        }
        return intervention;
      });
    }

    return {
      checkforDuplicateName: checkforDuplicateName,
      buildCovariateUsage: buildCovariateUsage,
      buildInterventionUsage: buildInterventionUsage,
      buildOutcomeUsage: buildOutcomeUsage,
      addMissingMultiplierInfo: addMissingMultiplierInfo
    };
  };
  return dependencies.concat(ProjectService);
});
