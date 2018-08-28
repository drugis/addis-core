'use strict';
define([], function() {
  var dependencies = [];
  var SpinnerDirective = function() {
    return {
      restrict: 'E',
      templateUrl: './spinnerDirective.html',
      transclude: true,
      scope: {
        promise: '=',
        size: '=',
        message: '='
      },
      link: function(scope) {
        scope.size = scope.size === 3 ? 3 : 1;
        scope.loading = {
          loaded: false
        };
        scope.$watch('promise', checkPromise);
        function checkPromise(){
          if (scope.promise) {
            scope.loading.loaded = false;
            scope.promise.then(function() {
              scope.loading.loaded = true;
            });
          }
        }
      }
    };
  };
  return dependencies.concat(SpinnerDirective);
});
