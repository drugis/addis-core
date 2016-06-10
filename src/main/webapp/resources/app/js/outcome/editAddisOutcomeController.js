'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$modalInstance', 'ProjectService', 'OutcomeResource', 'successCallback', 'outcome', 'outcomes'];
    var EditAddisOutcomeController = function($scope, $modalInstance, ProjectService, OutcomeResource, successCallback, outcome, outcomes) {
      $scope.outcome = angular.copy(outcome);

      $scope.saveOutcome = function() {
        $scope.isSaving = true;
        var editCommand = {
          name: $scope.outcome.name,
          motivation: $scope.outcome.motivation
        };
        OutcomeResource.save({
          projectId: $scope.outcome.project,
          outcomeId: $scope.outcome.id,
        }, editCommand, function() {
          $modalInstance.close();
          successCallback($scope.outcome.name, $scope.outcome.motivation);
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
