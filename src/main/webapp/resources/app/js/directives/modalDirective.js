'use strict';
define([], function() {
  var dependencies = [];
  var ModalDirective = function() {
    return {
      restrict: 'E',
      transclude: true,
      scope: {
        model: '=',
        buttonText: '@'
      },
      link: function(scope, element, attrs) {
        if (!scope.model) {
          scope.model = {};
        }
        scope.bgStyle = function(show) {
          return show ? {'display': 'block'} : {'display': 'none'};
        };
        scope.fgStyle = function(show) {
          return show ? {'display': 'block', 'visibility' : 'visible'} : {'display': 'none'};
        };

        scope.model.open = function() { scope.model.show = true; };
        scope.model.close = function() { scope.model.show = false; };
      },
      templateUrl: 'app/partials/modal.html'
    };
  };
  return dependencies.concat(ModalDirective);
});
