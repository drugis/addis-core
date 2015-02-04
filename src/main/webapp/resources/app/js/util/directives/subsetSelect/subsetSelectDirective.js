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
        scope.isSelected = [];
        scope.source.then(function(source) {
          scope.source = source;
          scope.isSelected = SubsetSelectService.createSelectionList(source, scope.target);
        });

        scope.updateSelection = function(isSelected, item) {
          scope.target = SubsetSelectService.addOrRemoveItem(isSelected, item, scope.target);
        };

      }
    };
  };
  return dependencies.concat(SubsetSelectDirective);
});