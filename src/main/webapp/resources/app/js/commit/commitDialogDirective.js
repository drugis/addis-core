'use strict';
define([], function() {
  var dependencies = ['$injector', 'GraphResource'];
  var CommitDialogDirective = function($injector, GraphResource) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/commit/commitDialogDirective.html',
      scope: {
        itemServiceName: '=',
        changesCommited: '=',
        commitCancelled: '=',
        datasetUuid: '=',
        graphUuid: '='
      },
      link: function(scope) {
        var ItemService = $injector.get(scope.itemServiceName);

        scope.commitChanges = function(commitTitle, commitDescription) {
          ItemService.getGraph().then(function(graph) {
            GraphResource.put({
              datasetUUID: scope.datasetUuid,
              graphUuid: scope.graphUuid,
              commitTitle: commitTitle,
              commitDescription: commitDescription
            }, graph.data, function(value, responseHeaders) {
              var newVersion = responseHeaders('X-EventSource-Version');
              newVersion = newVersion.split('/')[4];
              scope.changesCommited(newVersion);
            });
          });
        };
      }
    };
  };
  return dependencies.concat(CommitDialogDirective);
});