'use strict';
define(['lodash'], function(_) {
  var dependencies = ['ResultsService'];

  var SubsetSelectDirective = function(ResultsService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/variable/resultPropertiesDirective.html',
      scope: {
        variable: '='
      },
      link: function(scope) {
        scope.properties = buildProperties();
        if (scope.variable.measurementType === 'ontology:continuous') {
          scope.categories = buildCategories();
        }
        
        scope.hasNotAnalysedProperty = _.find(scope.properties, function(property) {
          return !property.analysisReady;
        });

        function buildProperties() {
          var variableTypeDetails = _.keyBy(_.filter(ResultsService.VARIABLE_TYPE_DETAILS, function(varType) {
            return varType.variableType === 'all' || varType.variableType === scope.variable.measurementType;
          }), 'type');
          scope.variable.selectedResultProperties.forEach(function(property) {
            variableTypeDetails[property.type].isSelected = true;
          });
          return variableTypeDetails;
        }

        function buildCategories() {
          var categories = _.keyBy(_.filter(ResultsService.VARIABLE_TYPE_DETAILS, function(varType) {
            return varType.variableType === 'all' || varType.variableType === scope.variable.measurementType;
          }), 'category');
          return _.reduce(categories, function(accum, category, key) {

            accum[key] = category;
            accum[key].properties = _.filter(scope.properties, function(property) {
                return property.category === key;
              })
              .map(function(property) {
                property.isSelected = !!_.find(scope.variable.selectedResultProperties, function(p) {
                  return p.type === property.type;
                });
                return property;
              });
            return accum;
          }, {});
        }

        scope.updateSelection = function() {
          scope.variable.selectedResultProperties = _.filter(scope.properties, function(property) {
            return property.isSelected;
          });
        };

        scope.$watch('variable.measurementType', function(newValue, oldValue) {
          if (!oldValue || !newValue || oldValue === newValue) {
            return;
          }
          scope.properties = buildProperties();
          if (newValue === 'ontology:continuous') {
            scope.categories = buildCategories();
          }
        });
        scope.$watch('variable.resultProperties', function(newValue, oldValue) {
          if (!oldValue || !newValue) {
            return;
          }
          scope.properties = buildProperties();
        });

      }
    };
  };
  return dependencies.concat(SubsetSelectDirective);
});
