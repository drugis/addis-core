'use strict';
define([], function() {
  var dependencies = ['SubsetSelectService'];

  var SubsetSelectDirective = function(SubsetSelectService) {
    return {
      restrict: 'E',
      templateUrl: './subsetSelectDirective.html',
      scope: {
        target: '=',
        source: '=',
        equals: '='
      },
      link: function(scope) {
        scope.isSelected = [];
        if(scope.source.then) {
          scope.source.then(initSelections);
        } else {
          initSelections(scope.source);
        }

        function initSelections(source) {
          scope.source = source;
          scope.isSelected = SubsetSelectService.createSelectionList(source, scope.target, scope.equals);
        }

        scope.updateSelection = function(isSelected, item) {
          scope.target = SubsetSelectService.addOrRemoveItem(isSelected, item, scope.target, scope.equals);
        };

        scope.$watch('target', function(oldValue, newValue) {
          if(!oldValue || !newValue) { return; }
          scope.isSelected = SubsetSelectService.createSelectionList(scope.source, scope.target, scope.equals);
        });

      }
    };
  };
  return dependencies.concat(SubsetSelectDirective);
});
