'use strict';
define([], function() {
  var dependencies = ['SubsetSelectService'];

  var SubsetSelectDirective = function(SubsetSelectService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/util/directives/subsetSelect/subsetSelectDirective.html',
      scope: {
        target: '=',
        source: '='
      },
      link: function(scope) {
        scope.isSelected = SubsetSelectService.createSelectionList(source, target);

        angular.forEach(scope.isSelected, function(selectedItem) {
          scope.$watch(
            function() { // watchExpression
              return selectedItem;
            },
            function(newValue, oldValue) { // listener
              if (newValue !== oldValue) {
                scope.target = SubsetSelectService.addOrRemoveValue(newValue, oldValue, target);
              }
            }
          );
        });
      }
    };
  };
  return dependencies.concat(SubsetSelectDirective);
});