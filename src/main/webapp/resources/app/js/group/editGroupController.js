'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback', 'item'];
    var EditGroupController = function($scope, $state, $modalInstance, itemService, callback, item) {

      $scope.isEditing = false;
      $scope.item = item;
      itemService.queryItems().then(function(groups) {
        $scope.otherGroups = _.filter(groups, function(group) {
          return group.groupUri !== $scope.item.groupUri;
        });
      });
      $scope.showMergeWarning = false;

      $scope.editItem = function() {
        $scope.isEditing = true;
        itemService.editItem(item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.reclassifyAsArm = function() {
        $scope.isEditing = true;
        itemService.reclassifyAsArm(item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.merge = function(targetGroup) {
        $scope.isEditing = true;
        itemService.merge(item, targetGroup).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.updateMergeWarning = function(targetGroup) {
        itemService.hasOverlap(item, targetGroup).then(function(result) {
          $scope.showMergeWarning = result;
        });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditGroupController);
  });
