'use strict';
define([], function() {
  var depencencies = [];
  var VersionInfoDirective = function() {
    return {
      restrict: 'E',
      transclude: true,
      templateUrl: 'app/js/dataset/versionInfoDirective.html'
    };
  };
  return depencencies.concat(VersionInfoDirective);
});
