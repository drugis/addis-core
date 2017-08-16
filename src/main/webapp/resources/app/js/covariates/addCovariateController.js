'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'CovariateResource', 'CovariateOptionsResource', '$modalInstance', 'callback'];
  var AddCovariateController = function($scope, $stateParams, CovariateResource, CovariateOptionsResource, $modalInstance, callback) {
    // functions
    $scope.addCovariate = addCovariate;
    $scope.cancel = cancel;
    $scope.alReadyAdded = alReadyAdded;

    // init
    $scope.covariatesOptions = CovariateOptionsResource.getProjectCovariates($stateParams);
    var selectedOptionsKeys = $scope.covariates.map(function(covariate) {
      return covariate.definitionKey;
    });

    function addCovariate(covariate) {
      $scope.isAddingCovariate = true;
      covariate.covariateDefinitionKey = covariate.definition.key;
      covariate.type = covariate.definition.typeKey;
      delete covariate.definition;
      covariate.projectId = $scope.project.id;
      CovariateResource.save(covariate, function() {
        callback();
        $modalInstance.close();
        $scope.isAddingCovariate = false;
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }

    function alReadyAdded(covariate) {
      return selectedOptionsKeys.indexOf(covariate.definition.key) >= 0;
    }


  };
  return dependencies.concat(AddCovariateController);
});