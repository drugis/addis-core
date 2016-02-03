'use strict';
define([], function() {
  var dependencies = ['$stateParams', '$modal', '$injector'];
  var CategoryDirective = function($stateParams, $modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/categoryDirective.html',
      scope: {
        studyUuid: '=',
        settings: '=',
        isEditingAllowed: '='
      },
      link: function(scope, element, attributes) {

        var refreshStudyDesignLister;
        var service = $injector.get(scope.settings.service);

        scope.isSingleItem = !!attributes.isSingleItem;

        scope.reloadItems = function() {
          console.log('CategoryDirective.reloadItems');
          if (refreshStudyDesignLister) {
            // stop listning while loading
            refreshStudyDesignLister();
          }

          service.queryItems($stateParams.studyUUID).then(function(queryResult) {
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
            templateUrl: scope.settings.addItemTemplateUrl,
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
