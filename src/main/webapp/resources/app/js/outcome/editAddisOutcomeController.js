'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$modalInstance', 'ProjectService', 'OutcomeResource', 'successCallback', 'outcome', 'outcomes', 'usage'];
    var EditAddisOutcomeController = function($scope, $modalInstance, ProjectService, OutcomeResource, successCallback, outcome, outcomes, usage) {
      $scope.outcome = angular.copy(outcome);
      $scope.usage = usage;

      $scope.saveOutcome = function() {
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
      };

      $scope.checkForDuplicateOutcomeName = function(name) {
        $scope.isDuplicateName = ProjectService.checkforDuplicateName(outcomes, name);
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditAddisOutcomeController);
  });
