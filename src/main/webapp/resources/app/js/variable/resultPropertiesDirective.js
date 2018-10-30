'use strict';
define(['lodash'], function(_) {
  var dependencies = ['ResultsService'];

  var ResultPropertiesDirective = function(ResultsService) {
    return {
      restrict: 'E',
      templateUrl: './resultPropertiesDirective.html',
      scope: {
        variable: '='
      },
      link: function(scope) {
        //functions
        scope.updateSelection = updateSelection;

        //init
        buildProperties();
        scope.hasNotAnalysedProperty = _.some(scope.properties, ['analysisReady', false]);
        var ARM_LEVEL = 'ontology:arm_level_data';

        // watches
        scope.$watch('variable.measurementType', rebuildIfNecessary);
        scope.$watch('variable.resultProperties', rebuildIfNecessary, true);
        scope.$watch('variable.armOrContrast', rebuildIfNecessary);

        function rebuildIfNecessary(newValue, oldValue) {
          if (!oldValue || !newValue || oldValue === newValue) {
            return;
          }
          buildProperties();
        }

        function buildProperties() {
          var resultPropertiesForType = ResultsService.getResultPropertiesForType(scope.variable.measurementType, scope.variable.armOrContrast);
          scope.properties = _(resultPropertiesForType)
            .map(setSelected)
            .keyBy('type')
            .value();
          setCategories();
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
          if (scope.variable.measurementType === 'ontology:continuous' && scope.variable.armOrContrast === ARM_LEVEL) {
            scope.categories = ResultsService.buildPropertyCategories(scope.variable);
            scope.showCategories = true;
          }
        }

        function updateSelection() {
          var properties = scope.properties;
          if (scope.variable.measurementType === 'ontology:continuous' && scope.variable.armOrContrast === ARM_LEVEL) {
            properties = _(scope.categories).map('properties').flatten().value();
          }
          setConfidenceInterval(properties);
          scope.variable.selectedResultProperties = _.filter(properties, 'isSelected');
        }

        function setConfidenceInterval(properties) {
          var hasConfidenceInterval = _.some(properties, function(property) {
            return property.type === 'confidence interval';
          });
          scope.variable.confidenceInterval = hasConfidenceInterval ? 95 : null;
        }
      }
    };
  };
  return dependencies.concat(ResultPropertiesDirective);
});
