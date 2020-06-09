'use strict';
define([], function() {
  var dependencies = [
    '$injector',
    'GraphResource',
    'UserService'
  ];
  var CommitDialogDirective = function(
    $injector,
    GraphResource,
    UserService
  ) {
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
        scope.commitChanges = commitChanges;
        scope.openLoginWindow = openLoginWindow;
        scope.updateButton = updateButton;

        checkLoggedInUser();

        function checkLoggedInUser() {
          return UserService.getLoginUser().then(function(loggedInUser) {
            scope.loggedInUser = loggedInUser;
            updateButton();
          });
        }

        function updateButton() {
          scope.commitDisabled = scope.isCommitting || !scope.commitTitle || !scope.loggedInUser;
        }

        function commitChanges(commitTitle, commitDescription) {
          checkLoggedInUser().then(function() {
            if (scope.loggedInUser) {
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
            }
          });
        }

        function openLoginWindow() {
          scope.commitDisabled = false;
          scope.loggedInUser = true;
          window.open('/', '', 'width=640, height=800');
        }
      }
    };
  };
  return dependencies.concat(CommitDialogDirective);
});
