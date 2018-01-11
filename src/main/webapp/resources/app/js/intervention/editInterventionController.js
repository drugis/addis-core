'use strict';
define(['angular'],
  function(angular) {
    var dependencies = ['$scope', '$modalInstance', 'ProjectService', 'InterventionResource', 'successCallback', 'intervention', 'interventions'];
    var EditInterventionController = function($scope, $modalInstance, ProjectService, InterventionResource, successCallback, intervention, interventions) {
      // functions
      $scope.saveIntervention = saveIntervention;
      $scope.checkForDuplicateInterventionName = checkForDuplicateInterventionName;
      $scope.cancel = cancel;

      // init
      $scope.intervention = angular.copy(intervention);

      function saveIntervention() {
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
      }

      function checkForDuplicateInterventionName(intervention) {
        $scope.isDuplicateName = ProjectService.checkforDuplicateName(interventions, intervention);
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditInterventionController);
  });