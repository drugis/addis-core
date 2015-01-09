'use strict';
define([], function() {
  var dependencies = ['$modal', '$injector'];

  var CategoryDirective = function($modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/categoryDirective.html',
      scope: {
        settings: '='
      },
      link: function(scope) {

        var service = $injector.get(scope.settings.service);

        scope.reloadItems = function() {
          service.queryItems().then(function(queryResult) {
            scope.items = queryResult.data.results.bindings;
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
              successCallback: function() {
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
