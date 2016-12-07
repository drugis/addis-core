'use strict';
define(['lodash'],
  function(_) {
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
        return buildDefinitionUsage(analyses, interventions, matcherFunction);
      }

      function buildOutcomeUsage(analyses, outcomes) {
        var matcherFunction = function(analysis, outcome) {
          return analysis.outcome.id === outcome.id;
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

      return {
        checkforDuplicateName: checkforDuplicateName,
        buildCovariateUsage: buildCovariateUsage,
        buildInterventionUsage: buildInterventionUsage,
        buildOutcomeUsage: buildOutcomeUsage
      };
    };
    return dependencies.concat(ProjectService);
  });
