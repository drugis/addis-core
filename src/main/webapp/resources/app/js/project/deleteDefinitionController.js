'use strict';
define([], function() {
  var dependencies = ['$scope', '$modalInstance', '$injector', 'definition', 'callback'];
  var DeleteDefinitionController = function($scope, $modalInstance, $injector, definition, callback) {
    var DEFINITIONS = {
      covariate: {
        resource: 'CovariateResource',
        coordinate: 'covariateId'
      },
      intervention: {
        resource: 'InterventionResource',
        coordinate: 'interventionId'
      },
      outcome: {
        resource: 'OutcomeResource',
        coordinate: 'outcomeId'
      }
    };

    $scope.deleteDefinition = deleteDefinition;
    $scope.definition = definition;

    function deleteDefinition() {
      var resource = $injector.get(DEFINITIONS[definition.definitionType].resource);
      var coordinates = {
        projectId: $scope.project.id
      };
      coordinates[DEFINITIONS[definition.definitionType].coordinate] = definition.id;
      resource.delete(coordinates).$promise.then(function() {
        callback();
        $modalInstance.close();
      });
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };

  return dependencies.concat(DeleteDefinitionController);

});
