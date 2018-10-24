'use strict';
define(['lodash'],
  function(_) {
    var dependencies = [
      '$scope',
      '$modalInstance',
      '$injector',
      'OutcomeService',
      'callback',
      'item'
    ];
    var EditOutcomeController = function(
      $scope,
      $modalInstance,
      $injector,
      OutcomeService,
      callback,
      item
    ) {
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
        OutcomeService.merge(item, targetOutcome, ItemService.TYPE)
          .then(succesCallback, $modalInstance.close);
      }

      function succesCallback() {
        callback();
        $modalInstance.close();
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
