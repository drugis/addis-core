'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$modalInstance', 'ProjectService', 'OutcomeResource', 'successCallback', 'outcome', 'outcomes', 'usage'];
    var EditAddisOutcomeController = function($scope, $modalInstance, ProjectService, OutcomeResource, successCallback, outcome, outcomes, usage) {
      // functions
      $scope.saveOutcome = saveOutcome;
      $scope.checkForDuplicateOutcomeName = checkForDuplicateOutcomeName;
      $scope.cancel = cancel;

      // init
      $scope.outcome = angular.copy(outcome);
      $scope.usage = usage;

      function saveOutcome() {
        $scope.isSaving = true;
        var editCommand = {
          name: $scope.outcome.name,
          motivation: $scope.outcome.motivation,
          direction: $scope.outcome.direction
        };
        OutcomeResource.save({
          projectId: $scope.outcome.project,
          outcomeId: $scope.outcome.id,
        }, editCommand, function() {
          $modalInstance.close();
          successCallback($scope.outcome.name, $scope.outcome.motivation, $scope.outcome.direction);
        });
      }

      function checkForDuplicateOutcomeName(name) {
        $scope.isDuplicateName = ProjectService.checkforDuplicateName(outcomes, name);
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditAddisOutcomeController);
  });