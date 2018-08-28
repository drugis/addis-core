'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$injector'];
  var EnumOptionsDirective = function($injector) {
    return {
      restrict: 'E',
      templateUrl: './enumOptionsDirective.html',
      scope: {
        editMode: '=',
        optionsConstantName: '=',
        selectedOption: '='
      },
      link: function(scope) {

        scope.enumOptions = _.values($injector.get(scope.optionsConstantName));

        if (scope.selectedOption) {
          scope.selectedOption = _.find(scope.enumOptions, function(option) {
            if (scope.selectedOption.uri) {
              return option.uri === scope.selectedOption.uri;
            } else {
              return option.uri === scope.selectedOption;
            }
          });
        }

        scope.onOptionChange = function(newOption) {
          if (newOption) {
            scope.selectedOption = newOption;
          } else {
            scope.selectedOption = undefined;
          }
        };

      }
    };
  };
  return dependencies.concat(EnumOptionsDirective);
});
