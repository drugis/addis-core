'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback', 'item', '$injector', 'OutcomeService'];
    var EditOutcomeController = function($scope, $state, $modalInstance, itemService, callback, item, $injector, OutcomeService) {
      // functions
      $scope.merge = merge;
      $scope.updateMergeWarning = updateMergeWarning;
      $scope.cancel = cancel;

      // init
      $scope.isEditing = false;
      $scope.item = item;
      var ItemService = $injector.get($scope.settings.service);

      ItemService.queryItems().then(function(outcomes) {
        $scope.otherOutcomes = _.filter(outcomes, function(outcome) {
          return outcome.uri !== $scope.item.uri;
        });
      });
      $scope.showMergeWarning = false;

      function merge(targetOutcome) {
        $scope.isEditing = true;
        OutcomeService.merge(item, targetOutcome, ItemService.TYPE).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.close();
          });
      }

      function updateMergeWarning(targetOutcome) {
        $scope.showDifferentTypeWarning = OutcomeService.hasDifferentType(item, targetOutcome);
        OutcomeService.hasOverlap(item, targetOutcome).then(function(result) {
          $scope.showMergeWarning = result;
        });
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditOutcomeController);
  });