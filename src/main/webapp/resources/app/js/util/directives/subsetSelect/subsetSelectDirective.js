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
        scope.isSelected = SubsetSelectService.createSelectionList(scope.source, scope.target);

        scope.updateSelection = function(isSelected, item) {
          scope.target = SubsetSelectService.addOrRemoveItem(isSelected, item, scope.target);
        };

      }
    };
  };
  return dependencies.concat(SubsetSelectDirective);
});