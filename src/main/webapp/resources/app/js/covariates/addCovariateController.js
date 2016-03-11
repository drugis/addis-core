'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'CovariateResource', 'CovariateOptionsResource','$modalInstance', 'callback'];
  var AddCovariateController = function($scope, $stateParams, CovariateResource, CovariateOptionsResource, $modalInstance, callback) {

    $scope.covariatesOptions = CovariateOptionsResource.getProjectCovariates($stateParams);
    var selectedOptionsKeys = $scope.covariates.map(function(covariate) {
      return covariate.definitionKey;
    });

    $scope.addCovariate = function(covariate) {
      $scope.isAddingCovariate = true;
      covariate.covariateDefinitionKey = covariate.definition.key;
      delete covariate.definition;
      covariate.projectId = $scope.project.id
      CovariateResource.save(covariate, function(result, headers) {
        callback();
        $modalInstance.close();
        $scope.isAddingCovariate = false;
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.alReadyAdded = function(covariate) {
      return selectedOptionsKeys.indexOf(covariate.definition.key) >= 0;
    }


  };
  return dependencies.concat(AddCovariateController);
});
