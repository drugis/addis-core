'use strict';
define([], function() {
  var dependencies = ['$stateParams', '$modal', '$injector'];
  var CategoryDirective = function($stateParams, $modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: './categoryDirective.html',
      scope: {
        studyUuid: '=',
        settings: '=',
        isEditingAllowed: '=',
        isRepairable: '='
      },
      link: function(scope, element, attributes) {

        var refreshStudyDesignLister;
        var service = $injector.get(scope.settings.service);

        scope.isSingleItem = !!attributes.isSingleItem;

        scope.reloadItems = function() {
          if (refreshStudyDesignLister) {
            // stop listning while loading
            refreshStudyDesignLister();
          }

          service.queryItems().then(function(queryResult) {
            scope.items = queryResult;

            refreshStudyDesignLister = scope.$on('refreshStudyDesign', function() {
              scope.reloadItems();
            });
          });
        };

        scope.reloadItems();

        function onAdd() {
          scope.$emit('updateStudyDesign');
          scope.reloadItems();
        }

        scope.addItem = function() {
          $modal.open({
            template: scope.settings.addItemtemplate,
            scope: scope,
            controller: scope.settings.addItemController,
            resolve: {
              callback: function() {
                return onAdd;
              },
              actionType: function() {
                return 'Add';
              }
            }
          });
        };
      }
    };
  };
  return dependencies.concat(CategoryDirective);
});
