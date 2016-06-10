'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$state', '$modalInstance', 'itemService', 'callback', 'item', '$injector', 'OutcomeService'];
    var EditOutcomeController = function($scope, $state, $modalInstance, itemService, callback, item, $injector, OutcomeService) {

      $scope.item = item;
      $injector.get($scope.settings.service).queryItems().then(function(outcomes) {
        $scope.otherOutcomes = _.filter(outcomes, function(outcome) {
          return outcome.uri !== $scope.item.uri;
        });
      });
      $scope.showMergeWarning = false;

      $scope.merge = function(targetOutcome) {
        $scope.isMerging = true;
        OutcomeService.merge(item, targetOutcome, $injector.get($scope.settings.service).TYPE).then(function() {
            callback();
            $modalInstance.close();
          },
          function() {
            $modalInstance.dismiss('cancel');
          });
      };

      $scope.updateMergeWarning = function(targetOutcome) {
        $scope.showDifferentTypeWarning = OutcomeService.hasDifferentType(item, targetOutcome);
        OutcomeService.hasOverlap(item, targetOutcome).then(function(result) {
          $scope.showMergeWarning = result;
        });
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditOutcomeController);
  });
