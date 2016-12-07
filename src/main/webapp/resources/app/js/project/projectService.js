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
        return _.reduce(covariates, function(accum, covariate) {
          accum[covariate.id] = _.map(_.filter(analyses, function(analysis) {
            return _.find(analysis.includedCovariates, ['covariateId', covariate.id]);
          }), 'title');
          return accum;
        }, {});
      }

      return {
        checkforDuplicateName: checkforDuplicateName,
        buildCovariateUsage: buildCovariateUsage
      };
    };
    return dependencies.concat(ProjectService);
  });
