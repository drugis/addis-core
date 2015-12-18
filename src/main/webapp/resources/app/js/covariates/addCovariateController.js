'use strict';
define([], function() {
  var dependencies = ['$scope', 'CovariateResource', 'CovariateOptionsResource','$modalInstance', 'callback'];
  var AddCovariateController = function($scope, CovariateResource, CovariateOptionsResource, $modalInstance, callback) {

    $scope.covariatesOptions = CovariateOptionsResource.query();
    var selectedOptionsKeys = _.pluck($scope.covariates, 'definitionKey');

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
      return _.contains(selectedOptionsKeys, covariate.definition.key);
    }


  };
  return dependencies.concat(AddCovariateController);
});