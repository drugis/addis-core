'use strict';
define([], function() {
  var dependencies = ['$injector', 'GraphResource'];
  var CommitDialogDirective = function($injector, GraphResource) {
    return {
      restrict: 'E',
      templateUrl: './commitDialogDirective.html',
      scope: {
        itemServiceName: '=',
        changesCommited: '=',
        commitCancelled: '=',
        userUid: '=',
        datasetUuid: '=',
        graphUuid: '='
      },
      link: function(scope) {
        var ItemService = $injector.get(scope.itemServiceName);

        scope.commitChanges = function(commitTitle, commitDescription) {
          scope.isCommitting = true;
          ItemService.getGraphAndContext().then(function(graph) {
            GraphResource.putJson({
              userUid: scope.userUid,
              datasetUuid: scope.datasetUuid,
              graphUuid: scope.graphUuid,
              commitTitle: commitTitle,
              commitDescription: commitDescription
            }, graph, function(value, responseHeaders) {
              var newVersion = responseHeaders('X-EventSource-Version');
              newVersion = newVersion.split('/versions/')[1];
              scope.changesCommited(newVersion);
            });
          });
        };

      }
    };
  };
  return dependencies.concat(CommitDialogDirective);
});
