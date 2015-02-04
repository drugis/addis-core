'use strict';
define([], function() {
  var dependencies = ['$modal', '$injector'];

  var CategoryItemDirective = function($modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/categoryItemDirective.html',
      scope: {
        item: '=',
        reloadItems: '=',
        settings: '='
      },
      link: function(scope) {

        var service = $injector.get(scope.settings.service);

        scope.editItem = function() {
          $modal.open({
            templateUrl: scope.settings.editItemTemplateUrl,
            scope: scope,
            controller: scope.settings.editItemController,
            resolve: {
              callback: function() {
                return scope.reloadItems;
              },
              itemService: function() {
                return service;
              },
              actionType: function() {
                return 'Edit'
              }
            }
          });
        };

        scope.deleteItem = function() {
          service.deleteItem(scope.item)
            .then(scope.reloadItems);
        };
      }


    };
  };

  return dependencies.concat(CategoryItemDirective);
});
