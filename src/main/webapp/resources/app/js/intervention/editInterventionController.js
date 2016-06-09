'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$modalInstance', 'ProjectService', 'InterventionResource', 'successCallback', 'intervention', 'interventions'];
    var EditInterventionController = function($scope, $modalInstance, ProjectService, InterventionResource, successCallback, intervention, interventions) {
      $scope.intervention = angular.copy(intervention);

      $scope.saveIntervention = function() {
        $scope.isSaving = true;
        var editCommand = {
          projectId: $scope.intervention.project,
          interventionId: $scope.intervention.id,
          name: $scope.intervention.name,
          motivation: $scope.intervention.motivation
        };
        InterventionResource.save(editCommand, function(){
          $modalInstance.close();
          successCallback($scope.intervention.name, $scope.intervention.motivation);
        });
      };

      $scope.checkForDuplicateInterventionName = function(name) {
        $scope.isDuplicateName = ProjectService.checkforDuplicateName(interventions, name);
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditInterventionController);
  });
