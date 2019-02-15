'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    'ResultPropertiesService',
    'ARM_LEVEL_TYPE',
    'CONTRAST_TYPE'
  ];
  var ResultPropertiesDirective = function(
    ResultPropertiesService,
    ARM_LEVEL_TYPE,
    CONTRAST_TYPE
  ) {
    return {
      restrict: 'E',
      templateUrl: './resultPropertiesDirective.html',
      scope: {
        variable: '='
      },
      link: function(scope) {
        //functions
        scope.updateSelection = updateSelection;
        scope.checkConfidenceInterval = checkConfidenceInterval;

        //init
        buildProperties();
        scope.hasNotAnalysedProperty = _.some(scope.properties, ['analysisReady', false]);

        // watches
        scope.$watch('variable.measurementType', rebuildIfNecessary);
        scope.$watch('variable.resultProperties', rebuildIfNecessary, true);

        function rebuildIfNecessary(newValue, oldValue) {
          if (!oldValue || !newValue || _.isEqual(oldValue, newValue)) {
            return;
          }
          buildProperties();
        }

        function buildProperties() {
          var resultPropertiesForType = ResultPropertiesService.getResultPropertiesForType(scope.variable.measurementType, scope.variable.armOrContrast);
          scope.properties = _(resultPropertiesForType)
            .map(setSelected)
            .filter(filterNonLogContrastStandardError)
            .keyBy('type')
            .value();
          setCategories();
        }

        function filterNonLogContrastStandardError(property){
          return property.armOrContrast !== CONTRAST_TYPE || 
          scope.variable.type === 'mean_difference' ||
          scope.variable.type === 'standardized_mean_difference' ||
          scope.variable.isLog ||
          property.type !== 'standard_error';
        }

        function setSelected(property) {
          property.isSelected = isPropertySelected(property);
          return property;
        }

        function isPropertySelected(property) {
          return _.some(scope.variable.selectedResultProperties, function(selectedProperty) {
            return selectedProperty.type === property.type;
          });
        }

        function setCategories() {
          scope.showCategories = false;
          if (scope.variable.measurementType === 'ontology:continuous' && scope.variable.armOrContrast === ARM_LEVEL_TYPE) {
            scope.categories = ResultPropertiesService.buildPropertyCategories(scope.variable);
            scope.showCategories = true;
          }
        }

        function updateSelection() {
          var properties = scope.properties;
          if (scope.variable.measurementType === 'ontology:continuous' && scope.variable.armOrContrast === ARM_LEVEL_TYPE) {
            properties = _(scope.categories).map('properties').flatten().value();
          }
          setConfidenceIntervalWidth(properties);
          scope.variable.selectedResultProperties = _.filter(properties, 'isSelected');
        }

        function setConfidenceIntervalWidth(properties) {
          var hasConfidenceIntervalWidth = _.some(properties, function(property) {
            return property.type === 'confidence_interval';
          });
          scope.variable.confidenceIntervalWidth = hasConfidenceIntervalWidth ? 95 : null;
        }

        function checkConfidenceInterval() {
          var value = scope.variable.confidenceIntervalWidth;
          if (value > 100 || value < 0 || value === undefined) {
            scope.variable.confidenceIntervalWidth = 95;
          }
        }
      }
    };
  };
  return dependencies.concat(ResultPropertiesDirective);
});
