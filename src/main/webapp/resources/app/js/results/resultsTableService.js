'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    'ResultsService'
  ];
  var ResultsTableService = function(
    ResultsService
  ) {

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

    function findCategoricalResult(resultValueObjects, category) {
      var resultValueObjectFound = _.find(resultValueObjects, function(resultValueObject) {
        return resultValueObject.result_property.category === category['@id'];
      });
      if (resultValueObjectFound) {
        if (resultValueObjectFound.value === undefined) {
          return undefined;
        } else {
          return Number(resultValueObjectFound.value);
        }
      }
    }

    function createInputColumns(variable, valueObjects) {
      if (!variable.resultProperties) {
        if (variable.categoryList) {
          return createCategoryInputColumn(variable, valueObjects);
        } else {
          return [];
        }
      }
      return createNonCategoricalInputColumn(variable, valueObjects);
    }

    function createNonCategoricalInputColumn(variable, valueObjects) {
      return _(variable.resultProperties)
        .reduce(function(accum, property) {
          var details = ResultsService.getVariableDetails(property, variable.armOrContrast);
          if (details.type === 'confidence_interval') {
            return accum.concat(createConfidenceIntervalColumns(property, details, valueObjects));
          } else {
            return accum.concat({
              resultProperty: property,
              valueName: details.label,
              value: findResultValueByType(valueObjects, details.type),
              dataType: details.dataType,
              isInValidValue: false,
              isAlwaysPositive: details.isAlwaysPositive,
              armOrContrast: details.armOrContrast
            });
          }
        }, []);
    }

    function createConfidenceIntervalColumns(property, details, valueObjects) {
      return [{
        resultProperty: property,
        valueName: details.label + ' lowerbound',
        value: findResultValueByType(valueObjects, details.type + ' lowerbound'),
        dataType: details.dataType,
        isInValidValue: false,
        armOrContrast: details.armOrContrast
      }, {
        resultProperty: property,
        valueName: details.label + ' upperbound',
        value: findResultValueByType(valueObjects, details.type + ' upperbound'),
        dataType: details.dataType,
        isInValidValue: false,
        armOrContrast: details.armOrContrast
      }];
    }

    function createCategoryInputColumn(variable, valueObjects) {
      return _.map(variable.categoryList, function(category) {
        var value;
        if (category.label) { // new format
          value = findCategoricalResult(valueObjects, category);
        } else { // old format
          value = findResultValueByType(valueObjects, category);
        }
        return {
          resultProperty: category,
          valueName: category,
          value: value,
          dataType: ResultsService.INTEGER_TYPE,
          isCategory: true,
          isInValidValue: false
        };
      });
    }

    function createHeaders(variable) {
      if (!variable.resultProperties) {
        return createCategoricalHeader(variable);
      } else {
        return createNonCategoricalHeaders(variable);
      }
    }

    function createNonCategoricalHeaders(variable) {
      return _.flatten(_.map(variable.resultProperties, function(property) {
        var propertyDetails = getPropertyDetails(property, variable);
        if (propertyDetails.type === 'confidence_interval') {
          return createConfidenceIntervalHeaders(propertyDetails, variable);
        } else {
          return createHeader(propertyDetails, variable);
        }
      }));
    }

    function createConfidenceIntervalHeaders(details, variable) {
      return [{
        label: 'confidence interval (' + variable.confidenceIntervalWidth + '%) lowerbound',
        lexiconKey: details.lexiconKey,
        analysisReady: details.analysisReady
      }, {
        label: 'confidence interval (' + variable.confidenceIntervalWidth + '%) upperbound',
        lexiconKey: details.lexiconKey,
        analysisReady: details.analysisReady
      }];
    }

    function createCategoricalHeader(variable) {
      if (!variable.categoryList) {
        return [];
      } else {
        //Categorical measurement
        if (variable.categoryList[0] && variable.categoryList[0].label) {
          return _.map(variable.categoryList, function(list) {
            return {
              'label': list.label
            };
          });
        } else {
          return variable.categoryList;
        }
      }
    }

    function createHeader(propertyDetails, variable) {
      var label = createHeaderLabel(propertyDetails, variable);
      return {
        label: label,
        lexiconKey: propertyDetails.lexiconKey,
        analysisReady: propertyDetails.analysisReady
      };
    }

    function getPropertyDetails(resultProperty, variable) {
      var propertyUri = _.isString(resultProperty) ? resultProperty : resultProperty.uri;
      return ResultsService.getVariableDetails(propertyUri, variable.armOrContrast);
    }

    function createHeaderLabel(propertyDetails, variable) {
      var scaleStrings = {
        P1D: ' (days)',
        P1W: ' (weeks)',
        P1M: ' (months)',
        P1Y: ' (years)'
      };
      var addition = (propertyDetails.type === 'exposure' ? scaleStrings[variable.timeScale] : '');
      return propertyDetails.label + addition;
    }

    function createInputRows(variable, arms, groups, measurementMoments, resultValuesObjects) {
      var rows = _(variable.measuredAtMoments)
        .reduce(function(accum, measuredAtMoment) {
          var measurementMoment = findMeasurementMoment(measurementMoments, measuredAtMoment);
          var armRows = createArmRows(arms, resultValuesObjects, measurementMoment, variable, groups);
          var groupRows = createGroupRows(arms, resultValuesObjects, measurementMoment, variable, groups);
          return accum.concat(armRows, groupRows);
        }, []);
      var sortedRows = sortRows(rows);
      return sortedRows;
    }

    function createGroupRows(arms, resultValuesObjects, measurementMoment, variable, groups) {
      return _.map(groups, function(group) {
        var valueObjects = filterRowValuesObjects(resultValuesObjects, measurementMoment.uri, group.groupUri);
        return createRow(variable, group, arms.length + groups.length, measurementMoment, valueObjects);
      });
    }

    function createArmRows(arms, resultValuesObjects, measurementMoment, variable, groups) {
      return _.map(arms, function(arm) {
        var valueObjects = filterRowValuesObjects(resultValuesObjects, measurementMoment.uri, arm.armURI);
        return createRow(variable, arm, arms.length + groups.length, measurementMoment, valueObjects);
      });
    }

    function filterRowValuesObjects(resultValuesObjects, measurementMomentUri, uri) {
      return _.filter(resultValuesObjects, function(resultValueObject) {
        return (resultValueObject.momentUri === measurementMomentUri && resultValueObject.armUri === uri);
      });
    }

    function findMeasurementMoment(measurementMoments, measuredAtMoment) {
      return _.find(measurementMoments, function(measurementMoment) {
        return measurementMoment.uri === measuredAtMoment.uri;
      });
    }

    function createRow(variable, group, numberOfGroups, measurementMoment, valueObjects) {
      var row = {
        variable: variable,
        group: group,
        measurementMoment: measurementMoment,
        numberOfGroups: numberOfGroups,
        inputColumns: createInputColumns(variable, valueObjects)
      };
      // if this row has any values set we need to save the instance uri on the row to use for update or delete
      if (valueObjects && valueObjects.length > 0) {
        row.uri = valueObjects[0].instance;
      }
      return row;
    }

    function sortRows(rows) {
      return rows.sort(function(row1, row2) {
        if (row1.measurementMoment.label.localeCompare(row2.measurementMoment.label) !== 0) {
          return row1.measurementMoment.label.localeCompare(row2.measurementMoment.label);
        }
        if (row1.group.label === 'Overall population') {
          return 1;
        }
        if (row2.group.label === 'Overall population') {
          return -1;
        }
        return row1.group.label.localeCompare(row2.group.label);
      });
    }

    function isValidValue(inputColumn) {
      if (inputColumn.value === undefined) {
        return false;
      }
      if (inputColumn.dataType === ResultsService.BOOLEAN_TYPE) {
        return typeof (inputColumn.value) === typeof (true);
      }

      if (inputColumn.value) {
        if (inputColumn.dataType === ResultsService.INTEGER_TYPE) {
          return Number.isInteger(inputColumn.value) && (!inputColumn.isAlwaysPositive || inputColumn.value >= 0);
        } else if (inputColumn.dataType === ResultsService.DOUBLE_TYPE) {
          return !isNaN(filterFloat(inputColumn.value)) && (!inputColumn.isAlwaysPositive || inputColumn.value >= 0);
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

    function buildMeasurementMomentOptions(measurementMoments) {
      //Builds the list of options per measurement moment, used when changing the mm of data to unassigned or another mm
      return _.reduce(measurementMoments, function(accum, measurementMoment) {
        var options = _.reject(measurementMoments, ['uri', measurementMoment.uri])
          .sort(function(mm1, mm2) {
            return mm1.label.localeCompare(mm2.label);
          })
          .concat({
            label: 'Unassign'
          });
        accum[measurementMoment.uri] = options;
        return accum;
      }, {});
    }

    function findOverlappingMeasurements(targetMMUri, inputRows) {
      return _.find(inputRows, function(inputRow) {
        return targetMMUri === inputRow.measurementMoment.uri && _.find(inputRow.inputColumns, function(inputColumn) {
          return inputColumn.value !== undefined;
        });
      });
    }

    return {
      createInputRows: createInputRows,
      createHeaders: createHeaders,
      isValidValue: isValidValue,
      createInputColumns: createInputColumns,
      buildMeasurementMomentOptions: buildMeasurementMomentOptions,
      findOverlappingMeasurements: findOverlappingMeasurements,
      CONTINUOUS_TYPE: CONTINUOUS_TYPE,
      DICHOTOMOUS_TYPE: DICHOTOMOUS_TYPE,
      CATEGORICAL_TYPE: CATEGORICAL_TYPE
    };
  };
  return dependencies.concat(ResultsTableService);
});
