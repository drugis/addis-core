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

        scope.commitChanges = function() {
          ItemService.getGraph().then(function(graph) {
            GraphResource.put({
              datasetUUID: scope.datasetUuid,
              graphUuid: scope.graphUuid
            }, graph.data, function() {
              scope.changesCommited();
            });
          });
        }
      }
    };
  };
  return dependencies.concat(CommitDialogDirective);
});