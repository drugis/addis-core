'use strict';
define([], function() {
  var dependencies = ['$stateParams', '$modal', '$injector'];
  var CategoryDirective = function($stateParams, $modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/categoryDirective.html',
      scope: {
        settings: '='
      },
      link: function(scope) {

        var service = $injector.get(scope.settings.service);

        scope.reloadItems = function() {
          service.queryItems($stateParams.studyUUID).then(function(queryResult) {
            scope.items = queryResult;
            console.log('category items retrieved. ' + queryResult.length);
          });
        };

        scope.reloadItems();

        scope.addItem = function() {
          $modal.open({
            templateUrl: scope.settings.addItemTemplateUrl,
            scope: scope,
            controller: scope.settings.addItemController,
            resolve: {
              callback: function() {
                return scope.reloadItems;
              }
            }
          });
        };
      }
    };
  };
  return dependencies.concat(CategoryDirective);
});
