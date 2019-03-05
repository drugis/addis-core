'use strict';
define([], function() {
  var dependencies = [
    '$modal',
    '$injector'
  ];
  var CategoryDirective = function(
    $modal,
    $injector
  ) {
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
        // functions
        scope.reloadItems = reloadItems;
        scope.addItem = addItem;

        // init
        var refreshStudyDesignLister;
        var service = $injector.get(scope.settings.service);
        scope.isSingleItem = !!attributes.isSingleItem;
        scope.reloadItems();

        function onAdd() {
          scope.$emit('updateStudyDesign');
          scope.reloadItems();
        }

        function reloadItems() {
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
        }

        function addItem() {
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
        }
      }
    };
  };
  return dependencies.concat(CategoryDirective);
});
