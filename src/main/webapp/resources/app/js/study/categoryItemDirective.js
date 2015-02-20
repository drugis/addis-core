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

        function onEdit() {
          scope.$emit('updateStudyDesign');
          scope.reloadItems();
        }

        scope.editItem = function() {
          $modal.open({
            templateUrl: scope.settings.editItemTemplateUrl,
            scope: scope,
            controller: scope.settings.editItemController,
            resolve: {
              callback: function () {
                return onEdit;
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
          service.deleteItem(scope.item).then(function() {
            scope.$emit('updateStudyDesign');
            scope.reloadItems();
          });
        };
      }


    };
  };

  return dependencies.concat(CategoryItemDirective);
});
