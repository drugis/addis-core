'use strict';
define([], function() {
  var dependencies = [];
  var AlertDirective = function() {
    return {
      restrict: "E",
      transclude: true,
      replace: true,
      scope: {
        type: '@',
        close: '&'
      },
      link: function(scope, element, attrs) {
        scope.animatedClose = function() {
          $(element).fadeOut(400, function() {
            scope.close();
          });
        };
      },
      template: '<div class="alert-box {{type}}"><div class="alert-box-message" ng-transclude></div><a ng-click="animatedClose()" class="close">&times;</a></div>'
    };
  };
  return dependencies.concat(AlertDirective);
});
