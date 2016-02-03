'use strict';
define([],
  function() {
    var dependencies = [];
    var ResultsTableService = function() {

      var CONTINUOUS_TYPE = 'ontology:continuous';
      var DICHOTOMOUS_TYPE = 'ontology:dichotomous';

      var INTEGER_TYPE = '<http://www.w3.org/2001/XMLSchema#integer>';
      var DOUBLE_TYPE = '<http://www.w3.org/2001/XMLSchema#double>';

      var MEAN_TYPE = 'mean';
      var STANDARD_DEVIATION_TYPE = 'standard_deviation';
      var SAMPLE_SIZE_TYPE = 'sample_size';
      var COUNT_TYPE = 'count';

      function findResultValueByType(resultValueObjects, type) {
        var resultValueObjectFound = _.find(resultValueObjects, function(resultValueObject) {
          return resultValueObject.result_property === type
        });

        if (resultValueObjectFound) {
          return Number(resultValueObjectFound.value);
        }
      }

      function createInputColumns(variable, rowValueObjects) {
        if (variable.measurementType === CONTINUOUS_TYPE) {
          return [{
            valueName: 'mean',
            value: findResultValueByType(rowValueObjects, MEAN_TYPE),
            dataType: DOUBLE_TYPE,
            isInValidValue: false
          }, {
            valueName: 'standard_deviation',
            value: findResultValueByType(rowValueObjects, STANDARD_DEVIATION_TYPE),
            dataType: DOUBLE_TYPE,
            isInValidValue: false
          }, {
            valueName: 'sample_size',
            value: findResultValueByType(rowValueObjects, SAMPLE_SIZE_TYPE),
            dataType: INTEGER_TYPE,
            isInValidValue: false
          }];
        } else if (variable.measurementType === DICHOTOMOUS_TYPE) {
          return [{
            valueName: 'count',
            value: findResultValueByType(rowValueObjects, COUNT_TYPE),
            dataType: INTEGER_TYPE,
            isInValidValue: false
          }, {
            valueName: 'sample_size',
            value: findResultValueByType(rowValueObjects, SAMPLE_SIZE_TYPE),
            dataType: INTEGER_TYPE,
            isInValidValue: false
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

        // if this row has any values set we need to save the instance uri on the row to use for update or delete
        if (rowValueObjects && rowValueObjects.length > 0) {
          row.uri = rowValueObjects[0].instance;
        }

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

      function isValidValue(inputColumn) {
        if (inputColumn.value === undefined) {
          return false;
        }
        if (inputColumn.value) {
          if (inputColumn.dataType === INTEGER_TYPE) {
            return Number.isInteger(inputColumn.value);
          } else if (inputColumn.dataType === DOUBLE_TYPE) {
            return !isNaN(filterFloat(inputColumn.value));
          }
        } else {
          return true;
        }
      }

      var filterFloat = function(value) {
        if (/^(\-|\+)?([0-9]+(\.[0-9]+)?|Infinity)$/.test(value)) {
          return Number(value);
        }
        return NaN;
      }

      return {
        createInputRows: createInputRows,
        createHeaders: createHeaders,
        isValidValue: isValidValue,
        CONTINUOUS_TYPE: CONTINUOUS_TYPE,
        DICHOTOMOUS_TYPE: DICHOTOMOUS_TYPE
      };
    };
    return dependencies.concat(ResultsTableService);
  });
