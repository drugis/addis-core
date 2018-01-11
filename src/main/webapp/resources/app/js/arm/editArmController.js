'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback', 'item'];
    var EditArmController = function($scope, $state, $modalInstance, itemService, callback, item) {
      // functions
      $scope.editItem = editItem;
      $scope.reclassifyAsGroup = reclassifyAsGroup;
      $scope.merge = merge;
      $scope.updateMergeWarning = updateMergeWarning; 
      $scope.cancel = cancel; 

      // init
      $scope.isEditing = false;
      $scope.item = item;
      itemService.queryItems().then(function(arms) {
        $scope.otherArms = _.filter(arms, function(arm) {
          return arm.armURI !== $scope.item.armURI;
        });
      });
      $scope.showMergeWarning = false;

      function editItem() {
        $scope.isEditing = true;
        itemService.editItem(item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function reclassifyAsGroup() {
        $scope.isEditing = true;
        itemService.reclassifyAsGroup(item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function merge(targetArm) {
        $scope.isEditing = true;
        itemService.merge(item, targetArm).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function updateMergeWarning(targetArm) {
        itemService.hasOverlap(item, targetArm).then(function(result) {
          $scope.showMergeWarning = result;
        });
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditArmController);
  });
