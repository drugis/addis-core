'use strict';
define([], function() {
  var dependencies = ['UUIDService'];
  var searchResultDirective = function(UUIDService) {
    return {
      restrict: 'E',
      templateUrl: './searchResultDirective.html',
      scope: {
        result: '='
      },
      link: function(scope) {
        scope.studyRefParams = {
          userUid: scope.result.owner,
          datasetUuid: UUIDService.getUuidFromNamespaceUrl(scope.result.datasetUrl),
          versionUuid: scope.result.versionUuid,
          studyGraphUuid: UUIDService.getUuidFromNamespaceUrl(scope.result.graphUri),
        };
      }
    };
  };
  return dependencies.concat(searchResultDirective);
});
