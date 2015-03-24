'use strict';
define([], function() {
  var dependencies = ['$stateParams'];
  var CommitDialogDirective = function($stateParams) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/commit/commitDialogDirective.html',
      scope: {
        settings: '='
      },
      link: function(scope) {}
    };
  };
  return dependencies.concat(CommitDialogDirective);
});
