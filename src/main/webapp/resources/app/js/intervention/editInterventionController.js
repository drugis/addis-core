'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$modalInstance', 'ProjectService', 'InterventionResource', 'successCallback', 'intervention', 'interventions'];
    var EditInterventionController = function($scope, $modalInstance, ProjectService, InterventionResource, successCallback, intervention, interventions) {
      $scope.intervention = angular.copy(intervention);

      $scope.saveIntervention = function() {
        $scope.isSaving = true;
        var editCommand = {
          name: $scope.intervention.name,
          motivation: $scope.intervention.motivation
        };
        InterventionResource.save({
          projectId: $scope.intervention.project,
          interventionId: $scope.intervention.id,
        }, editCommand, function() {
          $modalInstance.close();
          successCallback($scope.intervention.name, $scope.intervention.motivation);
        });
      };

      $scope.checkForDuplicateInterventionName = function(intervention) {
        $scope.isDuplicateName = ProjectService.checkforDuplicateName(interventions, intervention);
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditInterventionController);
  });
