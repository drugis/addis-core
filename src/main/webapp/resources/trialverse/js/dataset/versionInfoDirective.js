'use strict';
define([], function() {
  var depencencies = ['$stateParams'];
  var VersionInfoDirective = function($stateParams) {
    return {
      restrict: 'E',
      transclude: true,
      templateUrl: 'trialverse/js/dataset/versionInfoDirective.html'
    };
  };
  return depencencies.concat(VersionInfoDirective);
});
