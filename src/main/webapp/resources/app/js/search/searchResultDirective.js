'use strict';
define([], function() {
  var dependencies = ['UUIDService'];
  var searchResultDirective = function(UUIDService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/search/searchResultDirective.html',
      scope: {
        result: '='
      },
      link: function(scope) {
        scope.studyRefParams = {
          userUid: scope.result.ownerUuid,
          datasetUUID: UUIDService.getUuidFromNamespaceUrl(scope.result.datasetUrl),
          versionUuid: scope.result.versionUuid,
          studyGraphUuid: UUIDService.getUuidFromNamespaceUrl(scope.result.graphUri),
        };
      }
    };
  };
  return dependencies.concat(searchResultDirective);
});
