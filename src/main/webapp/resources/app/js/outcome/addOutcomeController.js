'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$modalInstance', 'callback', 'OutcomeResource'];
  var AddOutcomeController = function($scope, $modalInstance, callback, OutcomeResource) {
    // functions
    $scope.checkForDuplicateOutcomeName = checkForDuplicateOutcomeName;
    $scope.cancel = cancel;
    $scope.addOutcome = addOutcome;

    // init
    $scope.newOutcome = {
      direction: 1
    };
    $scope.duplicateOutcomeName = {
      isDuplicate: false
    };
    $scope.isAddingOutcome = false;

    function addOutcome(newOutcome) {
      $scope.isAddingOutcome = true;
      newOutcome.projectId = $scope.project.id;
      newOutcome.semanticOutcomeLabel = newOutcome.semanticOutcome.label;
      OutcomeResource.save(newOutcome, function() {
        $modalInstance.close();
        callback(newOutcome);
        $scope.isAddingOutcome = false;
      });
    }

    function checkForDuplicateOutcomeName(name) {
      $scope.duplicateOutcomeName.isDuplicate = _.find($scope.outcomes, function(item) {
        return item.name === name;
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }

  };
  return dependencies.concat(AddOutcomeController);
});
