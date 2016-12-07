'use strict';
define([], function() {
  var dependencies = ['$scope', '$modalInstance', '$injector', 'definition', 'callback'];
  var DeleteDefinitionController = function($scope, $modalInstance, $injector, definition, callback) {
    var DEFINITIONS = {
      covariate: {
        resource: 'CovariateResource',
        coordinateBuilder: function() {
          return {
            projectId: $scope.project.id,
            covariateId: definition.id
          };
        }
      }
    };

    $scope.deleteDefinition = deleteDefinition;
    $scope.definition = definition;

    function deleteDefinition() {
      var resource = $injector.get(DEFINITIONS[definition.definitionType].resource);
      resource.delete(DEFINITIONS[definition.definitionType].coordinateBuilder()).$promise.then(function() {
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
