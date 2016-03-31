'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$modalInstance', 'callback', 'InterventionResource'];
  var AddInterventionController = function($scope, $modalInstance, callback, InterventionResource) {
    $scope.checkForDuplicateInterventionName = checkForDuplicateInterventionName;
    $scope.cancel = cancel;
    $scope.addIntervention = addIntervention;
    $scope.selectTab = selectTab;
    $scope.newIntervention = {};
    $scope.duplicateInterventionName = {
      isDuplicate: false
    };
    $scope.isAddingIntervention = false;
    $scope.activeTab = 'simple';

    function addIntervention(newIntervention) {
      $scope.isAddingIntervention = true;
      newIntervention.projectId = $scope.project.id;
      newIntervention.semanticInterventionLabel = newIntervention.semanticIntervention.label;
      InterventionResource.save(newIntervention, function() {
        $modalInstance.close();
        callback(newIntervention);
        $scope.isAddingIntervention = false;
      });
    }

    function checkForDuplicateInterventionName(name) {
      $scope.duplicateInterventionName.isDuplicate = _.find($scope.Interventions, function(item) {
        return item.name === name;
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }

    function selectTab(selectedTab) {
      $scope.newIntervention = {};
      $scope.activeTab = selectedTab;
    }

  };
  return dependencies.concat(AddInterventionController);
});
