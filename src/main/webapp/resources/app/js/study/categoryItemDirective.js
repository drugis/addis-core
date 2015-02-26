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
        studyUuid: '='
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
          service.deleteItem(scope.item, $stateParams.studyUUID).then(function() {
            scope.$emit('updateStudyDesign');
            scope.reloadItems();
          });
        };
      }


    };
  };

  return dependencies.concat(CategoryItemDirective);
});
