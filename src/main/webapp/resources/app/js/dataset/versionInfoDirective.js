'use strict';
define([], function() {
  var depencencies = [];
  var VersionInfoDirective = function() {
    return {
      restrict: 'E',
      transclude: true,
      templateUrl: './versionInfoDirective.html'
    };
  };
  return depencencies.concat(VersionInfoDirective);
});
