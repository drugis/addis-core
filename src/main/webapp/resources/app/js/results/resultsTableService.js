'use strict';
define([],
    function() {
      var dependencies = [];
      var ResultsTableService = function() {

        var CONTINUOUS_TYPE = 'http://trials.drugis.org/ontology#continuous';

        function buildRow(variable, arm, arms, measuredAtMoment, measurementMoments) {
          var row = {
            variable: variable,
            arm: arm,
            measurementMoment: _.find(measurementMoments, function(measurementMoment) {
              return measurementMoment.uri === measuredAtMoment.uri;
            }),
            nArms: arms.length,
            values: [{
              type: sample_size: null
            }]
          };
          if (variable.measurementType === CONTINUOUS_TYPE) {
            row.values = row.values.concat([{
                mean: null
              }, {
                standard_deviation: null
              }]; row.isContinuous = true;
            }
            return row;
          }

          function createInputRows(variable, arms, measurementMoments) {
            var result = [];
            _.forEach(variable.measuredAtMoments, function(measuredAtMoments) {
              _.forEach(arms, function(arm) {
                result = result.concat(buildRow(variable, arm, arms, measuredAtMoments, measurementMoments));
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
