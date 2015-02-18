'use strict';
define([],
  function() {
    var dependencies = [];
    var ResultsTableService = function() {

      var CONTINUOUS_TYPE = 'http://trials.drugis.org/ontology#continuous';
      var DICHOTOMOUS_TYPE = 'http://trials.drugis.org/ontology#dichotomous';

      var MEAN_TYPE = 'http://trials.drugis.org/ontology#mean';
      var STANDARD_DEVIATION_TYPE = 'http://trials.drugis.org/ontology#standard_deviation';
      var SAMPLE_SIZE_TYPE = 'http://trials.drugis.org/ontology#sample_size';
      var COUNT_TYPE = 'http://trials.drugis.org/ontology#count';

      function findResultValueByType(resultValueObjects, type) {
        var resultValueObjectFound = _.find(resultValueObjects, function(resultValueObject){
          return resultValueObject.result_property === type 
        });

        if(resultValueObjectFound){
          return Number(resultValueObjectFound.value);
        }
      }

      function createInputColumns(variable, rowValueObjects) {
        if (variable.measurementType === CONTINUOUS_TYPE) {
          return [{
              valueName: 'mean',
              value: findResultValueByType(rowValueObjects, MEAN_TYPE)
            }, {
              valueName: 'standard_deviation',
              value: findResultValueByType(rowValueObjects, STANDARD_DEVIATION_TYPE)
            },
            {
              valueName: 'sample_size',
              value: findResultValueByType(rowValueObjects, SAMPLE_SIZE_TYPE)
           }];
        } else if (variable.measurementType === DICHOTOMOUS_TYPE) {
          return [{
            valueName: 'count',
            value: findResultValueByType(rowValueObjects, COUNT_TYPE)
          }, {
            valueName: 'sample_size',
            value: findResultValueByType(rowValueObjects, SAMPLE_SIZE_TYPE)
          }];
        }
      }

      function createHeaders(measurementType) {
        if (measurementType === CONTINUOUS_TYPE) {
          return ['Mean', '± sd', 'N'];
        } else if (measurementType === DICHOTOMOUS_TYPE) {
          return ['Count', 'N'];
        }
      }

      function createRow(variable, arm, numberOfArms, measurementMoment, rowValueObjects) {
        var row = {
          variable: variable,
          arm: arm,
          measurementMoment: measurementMoment,
          numberOfArms: numberOfArms,
          inputColumns: createInputColumns(variable, rowValueObjects)
        };
        return row;
      }

      function createInputRows(variable, arms, measurementMoments, resultValuesObjects) {
        var rows = [];
        _.forEach(variable.measuredAtMoments, function(measuredAtMoment) {

          var measurementMoment = _.find(measurementMoments, function(measurementMoment) {
            return measurementMoment.uri === measuredAtMoment.uri;
          });

          _.forEach(arms, function(arm) {
            var rowValueObjects = _.filter(resultValuesObjects, function(resultValueObject) {
              return (resultValueObject.momentUri === measurementMoment.uri && resultValueObject.armUri === arm.armURI);
            })
            rows = rows.concat(createRow(variable, arm, arms.length, measurementMoment, rowValueObjects));
          });

        });
        return rows;
      }

      return {
        createInputRows: createInputRows,
        createHeaders: createHeaders,
        CONTINUOUS_TYPE: CONTINUOUS_TYPE,
        DICHOTOMOUS_TYPE: DICHOTOMOUS_TYPE
      };
    };
    return dependencies.concat(ResultsTableService);
  });
