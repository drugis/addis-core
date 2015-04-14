'use strict';
define([], function() {
  var depencencies = ['$stateParams'];
  var VersionInfoDirective = function($stateParams) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/dataset/versionInfoDirective.html'
    };
  };
  return depencencies.concat(VersionInfoDirective);
});