'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback', 'item'];
    var EditGroupController = function($scope, $state, $modalInstance, itemService, callback, item) {
      // functions
      $scope.editItem = editItem;
      $scope.reclassifyAsArm = reclassifyAsArm;
      $scope.merge = merge;
      $scope.updateMergeWarning = updateMergeWarning;
      $scope.cancel = cancel;

      // init
      $scope.isEditing = false;
      $scope.item = item;
      itemService.queryItems().then(function(groups) {
        $scope.otherGroups = _.filter(groups, function(group) {
          return group.groupUri !== $scope.item.groupUri;
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

      function reclassifyAsArm() {
        $scope.isEditing = true;
        itemService.reclassifyAsArm(item).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function merge(targetGroup) {
        $scope.isEditing = true;
        itemService.merge(item, targetGroup).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function updateMergeWarning(targetGroup) {
        itemService.hasOverlap(item, targetGroup).then(function(result) {
          $scope.showMergeWarning = result;
        });
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditGroupController);
  });