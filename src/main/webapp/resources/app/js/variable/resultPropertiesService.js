'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = [
    'RESULT_PROPERTY_TYPE_DETAILS',
    'DEFAULT_RESULT_PROPERTIES',
    'CONTRAST_OPTIONS_DETAILS',
    'ARM_LEVEL_TYPE',
    'CONTRAST_TYPE'
  ];
  var ResultPropertiesService = function(
    RESULT_PROPERTY_TYPE_DETAILS,
    DEFAULT_RESULT_PROPERTIES,
    CONTRAST_OPTIONS_DETAILS,
    ARM_LEVEL_TYPE,
    CONTRAST_TYPE
  ) {

    function getDefaultResultProperties(measurementType, armOrContrast) {
      var returnProperties = DEFAULT_RESULT_PROPERTIES[armOrContrast][measurementType];
      if (returnProperties) {
        return returnProperties;
      } else {
        console.error('unknown measurement type ' + measurementType);
      }
    }

    function getResultPropertiesForType(measurementType, armOrContrast) {
      return getResultProperties(measurementType, RESULT_PROPERTY_TYPE_DETAILS[armOrContrast]);
    }

    function getResultProperties(measurementType, typedetails) {
      return _.filter(typedetails, function(varType) {
        return varType.variableTypes === 'all' || varType.variableTypes.indexOf(measurementType) > -1;
      });
    }

    function buildPropertyCategories(variable) {
      var properties = getResultPropertiesForType(variable.measurementType, variable.armOrContrast);
      var categories = _(properties)
        .keyBy('category')
        .mapValues(function(property, categoryName) {
          var categoryProperties = _(properties)
            .filter(['category', categoryName])
            .map(function(property) {
              return _.extend({}, property, {
                isSelected: _.some(variable.selectedResultProperties, ['type', property.type])
              });
            }).value();
          return {
            categoryLabel: categoryName,
            properties: categoryProperties
          };
        })
        .value();
      return categories;
    }

    function setTimeScaleInput(variable) {
      var newVariable = angular.copy(variable);
      if (hasExposure(newVariable.selectedResultProperties)) {
        if (!newVariable.timeScale) {
          newVariable.timeScale = 'P1W';
        }
      } else {
        delete newVariable.timeScale;
      }
      return newVariable;
    }

    function hasExposure(resultProperties) {
      return _.some(resultProperties, ['uri', 'http://trials.drugis.org/ontology#exposure']);
    }

    function resetResultProperties(variable, arms) {
      var newVariable = angular.copy(variable);
      newVariable.armOrContrast = ARM_LEVEL_TYPE;
      newVariable = armOrContrastChanged(newVariable, arms);
      if (newVariable.measurementType === 'ontology:categorical') {
        newVariable.categoryList = [];
      } else {
        delete newVariable.categoryList;
      }
      return newVariable;
    }

    function armOrContrastChanged(variable, arms) {
      var newVariable = angular.copy(variable);
      newVariable.resultProperties = RESULT_PROPERTY_TYPE_DETAILS[newVariable.armOrContrast];
      newVariable.selectedResultProperties = getDefaultResultProperties(newVariable.measurementType, newVariable.armOrContrast);
      if (newVariable.armOrContrast === CONTRAST_TYPE) {
        newVariable.referenceArm = arms[0].armURI;
        newVariable.contrastOptions = getContrastOptions(variable.measurementType);
        newVariable.contrastOption = newVariable.contrastOptions[0];
        newVariable.isLog = newVariable.contrastOption.isLog;
      }
      return newVariable;
    }

    function getContrastOptions(measurementType) {
      return _.filter(CONTRAST_OPTIONS_DETAILS, function(option) {
        return _.includes(option.variableTypes, measurementType);
      });
    }

    return {
      getDefaultResultProperties: getDefaultResultProperties,
      getResultPropertiesForType: getResultPropertiesForType,
      buildPropertyCategories: buildPropertyCategories,
      setTimeScaleInput: setTimeScaleInput,
      resetResultProperties: resetResultProperties,
      armOrContrastChanged: armOrContrastChanged,
      getContrastOptions: getContrastOptions
    };
  };
  return dependencies.concat(ResultPropertiesService);
});
