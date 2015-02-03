'use strict';
define([], function() {
  var dependencies = [];

  var SubsetSelectDirective = function() {
    return {
      restrict: 'E',
      templateUrl: 'app/js/util/directives/subsetSelect/subsetSelectDirective.html',
      scope: {
        target: '=',
        source: '='
      },
      link: function(scope) {
        scope.isSelected = _.map(scope.source, function() {
          return false;
        });

        angular.forEach(scope.isSelected, function(selectedItem) {
          scope.$watch(
            function() { // watchExpression
              return selectedItem;
            },
            function(newValue, oldValue) { // listener
              if (newValue !== oldValue) {
                if (newValue) {
                  scope.target = scope.target.concat(newValue);
                } else {
                  scope.target = _.without(scope.target, oldValue);
                }
              }
            }
          );
        });
      }
    };
  };
  return dependencies.concat(SubsetSelectDirective);
});