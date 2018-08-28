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
        scope.properties = buildProperties();
        if (scope.variable.measurementType === 'ontology:continuous') {
          scope.categories = ResultsService.buildPropertyCategories(scope.variable);
        }
        scope.hasNotAnalysedProperty = _.find(scope.properties, function(property) {
          return !property.analysisReady;
        });

        // watches
        scope.$watch('variable.measurementType', function(newValue, oldValue) {
          if (!oldValue || !newValue || oldValue === newValue) {
            return;
          }
          scope.properties = buildProperties();
          if (newValue === 'ontology:continuous') {
            scope.categories = ResultsService.buildPropertyCategories(scope.variable, scope.properties);
          }
        });
        scope.$watch('variable.resultProperties', function(newValue, oldValue) {
          if (!oldValue || !newValue) {
            return;
          }
          scope.properties = buildProperties();
        });


        function buildProperties() {
          var variableTypeDetails = _.keyBy(ResultsService.getResultPropertiesForType(scope.variable.measurementType), 'type');
          scope.variable.selectedResultProperties.forEach(function(property) {
            variableTypeDetails[property.type].isSelected = true;
          });
          return variableTypeDetails;
        }


        function updateSelection() {
          var properties = scope.properties;
          if (scope.variable.measurementType === 'ontology:continuous') {
            properties = _.reduce(scope.categories, function(accum, category) {
              return accum.concat(category.properties);
            }, []);
          }
          scope.variable.selectedResultProperties = _.filter(properties, function(property) {
            return property.isSelected;
          });
        }
      }
    };
  };
  return dependencies.concat(ResultPropertiesDirective);
});
