'use strict';
define(['lodash'], function(_) {
  var dependencies = ['ResultsService'];
  var ResultsTableService = function(ResultsService) {

    var CONTINUOUS_TYPE = 'ontology:continuous';
    var DICHOTOMOUS_TYPE = 'ontology:dichotomous';
    var CATEGORICAL_TYPE = 'ontology:categorical';

    function findResultValueByType(resultValueObjects, type) {
      var resultValueObjectFound = _.find(resultValueObjects, function(resultValueObject) {
        return resultValueObject.result_property === type;
      });

      if (resultValueObjectFound) {
        return Number(resultValueObjectFound.value);
      }
    }

    function createInputColumns(variable, rowValueObjects) {
      if (!variable.resultProperties) {
        if (!variable.categoryList) {
          return [];
        } else {
          return variable.categoryList.map(function(category) {
            return {
              resultProperty: category,
              valueName: category,
              value: 0,
              dataType: ResultsService.INTEGER_TYPE,
              isInValidValue: false
            };
          });
        }
      }
      return variable.resultProperties.map(function(type) {
        var details = ResultsService.getVariableDetails(type);
        return {
          resultProperty: type,
          valueName: details.label,
          value: findResultValueByType(rowValueObjects, details.type),
          dataType: details.dataType,
          isInValidValue: false
        };
      });
    }

    function createHeaders(variable) {
      if (!variable.resultProperties) {
        if (!variable.categoryList) {
          return [];
        } else {
          return variable.categoryList;
        }
      }
      if (!variable.resultProperties.map) {
        return [ResultsService.getVariableDetails(variable.resultProperties).label];
      }
      return variable.resultProperties.map(function(type) {
        return ResultsService.getVariableDetails(type).label;
      });
    }

    function createRow(variable, group, numberOfGroups, measurementMoment, rowValueObjects) {
      var row = {
        variable: variable,
        group: group,
        measurementMoment: measurementMoment,
        numberOfGroups: numberOfGroups,
        inputColumns: createInputColumns(variable, rowValueObjects)
      };

      // if this row has any values set we need to save the instance uri on the row to use for update or delete
      if (rowValueObjects && rowValueObjects.length > 0) {
        row.uri = rowValueObjects[0].instance;
      }

      return row;
    }

    function createInputRows(variable, arms, groups, measurementMoments, resultValuesObjects) {
      var rows = [];
      _.forEach(variable.measuredAtMoments, function(measuredAtMoment) {

        var measurementMoment = _.find(measurementMoments, function(measurementMoment) {
          return measurementMoment.uri === measuredAtMoment.uri;
        });

        _.forEach(arms, function(arm) {
          var rowValueObjects = _.filter(resultValuesObjects, function(resultValueObject) {
            return (resultValueObject.momentUri === measurementMoment.uri && resultValueObject.armUri === arm.armURI);
          });
          rows = rows.concat(createRow(variable, arm, arms.length + groups.length, measurementMoment, rowValueObjects));
        });

        _.forEach(groups, function(group) {
          var rowValueObjects = _.filter(resultValuesObjects, function(resultValueObject) {
            return (resultValueObject.momentUri === measurementMoment.uri && resultValueObject.armUri === group.groupUri);
          });
          rows = rows.concat(createRow(variable, group, arms.length + groups.length, measurementMoment, rowValueObjects));
        });
      });
      return rows;
    }

    function isValidValue(inputColumn) {
      if (inputColumn.value === undefined) {
        return false;
      }
      if (inputColumn.value) {
        if (inputColumn.dataType === ResultsService.INTEGER_TYPE) {
          return Number.isInteger(inputColumn.value);
        } else if (inputColumn.dataType === ResultsService.DOUBLE_TYPE) {
          return !isNaN(filterFloat(inputColumn.value));
        }
      } else {
        return true;
      }
    }

    function filterFloat(value) {
      if (/^(\-|\+)?([0-9]+(\.[0-9]+)?|Infinity)$/.test(value)) {
        return Number(value);
      }
      return NaN;
    }

    return {
      createInputRows: createInputRows,
      createHeaders: createHeaders,
      isValidValue: isValidValue,
      createInputColumns: createInputColumns,
      CONTINUOUS_TYPE: CONTINUOUS_TYPE,
      DICHOTOMOUS_TYPE: DICHOTOMOUS_TYPE,
      CATEGORICAL_TYPE: CATEGORICAL_TYPE
    };
  };
  return dependencies.concat(ResultsTableService);
});
