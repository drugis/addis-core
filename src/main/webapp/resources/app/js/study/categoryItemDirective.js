'use strict';
define(['angular'], function(angular) {
  var dependencies = ['$modal', '$injector'];

  var CategoryItemDirective = function($modal, $injector) {
    return {
      restrict: 'E',
      templateUrl: './categoryItemDirective.html',
      scope: {
        item: '=',
        reloadItems: '=',
        settings: '=',
        studyUuid: '=',
        isEditingAllowed: '=',
        isSingleItem: '=',
        isRepairable: '=',
        arms: '=',
        groups: '=',
        measurementMoments: '='
      },
      link: function(scope) {

        var service = $injector.get(scope.settings.service);

        scope.$watch('item', function() {
          scope.isEditingAllowed = scope.isEditingAllowed && !scope.item.disableEditing;
        }, true);

        function onEdit() {
          scope.$emit('updateStudyDesign');
          scope.reloadItems();
        }

        scope.editItem = function() {
          $modal.open({
            scope: scope,
            template: scope.settings.editItemtemplate,  // small t due to webpack templateurl module
            controller: scope.settings.editItemController,
            resolve: {
              callback: function () {
                return onEdit;
              },
              itemType: function() {
                return scope.settings.itemName;
              },
              itemService: function() {
                return service;
              },
              actionType: function() {
                return 'Edit';
              },
              item: function() {
                return angular.copy(scope.item);
              }
            }
          });
        };

        scope.repairItem = function() {
          $modal.open({
            scope: scope,
            template: scope.settings.repairItemtemplate,
            controller: scope.settings.repairItemController,
            resolve: {
              callback: function () {
                return onEdit;
              },
              itemService: function() {
                return service;
              },
              actionType: function() {
                return 'Repair';
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
