'use strict';
define([],
  function() {
    var dependencies = [];
    var ResultsTableService = function() {

      function buildRow(arm, arms, measuredAtMoment, measurementMoments) {
        return {
          arm: arm,
          measurementMoment: _.find(measurementMoments, function(measurementMoment) {
            return measurementMoment.uri === measuredAtMoment;
          }),
          nArms: arms.length,
          n: null,
          mean: null,
          sd: null
        };
      }

      function createInputRows(variable, arms, measurementMoments) {
        var result = [];
        _.forEach(variable.measuredAtMoments, function(measuredAtMoments) {
          _.forEach(arms, function(arm) {
            result = result.concat(buildRow(arm, arms, measuredAtMoments, measurementMoments));
          });
        });
        return result;
      };

      return {
        createInputRows: createInputRows
      };
    };
    return dependencies.concat(ResultsTableService);
  });
