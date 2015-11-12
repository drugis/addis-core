'use strict';
define([], function() {
  var dependencies = ['$stateParams', '$modal', '$injector'];

  var CategoryItemDirective = function($stateParams, $modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/categoryItemDirective.html',
      scope: {
        item: '=',
        reloadItems: '=',
        settings: '=',
        studyUuid: '=',
        isEditingAllowed: '=',
        isSingleItem: '='
      },
      link: function(scope, element, attibutes) {

        var service = $injector.get(scope.settings.service);

        function onEdit() {
          scope.$emit('updateStudyDesign');
          scope.reloadItems();
        }

        scope.editItem = function() {
          $modal.open({
            scope: scope,
            templateUrl: scope.settings.editItemTemplateUrl,
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
              },
              item: function() {
                return angular.copy(scope.item);
              }
            }
          });
        };

        scope.deleteItem = function() {
          service.deleteItem(scope.item, scope.studyUuid).then(function() {
            scope.$emit('updateStudyDesign');
            scope.reloadItems();
          });
        };
      }


    };
  };

  return dependencies.concat(CategoryItemDirective);
});
