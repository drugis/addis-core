'use strict';
define([], function() {
  var dependencies = ['$scope', 'CovariateResource', 'CovariateOptionsResource','$modalInstance'];
  var AddCovariateController = function($scope, CovariateResource, CovariateOptionsResource, $modalInstance) {

    $scope.covariates = CovariateOptionsResource.query();

    $scope.addCovariate = function(covariate) {
      $scope.isAddingCovariate = true;
      CovariateResource.save(covariate, function(result, headers) {
        $modalInstance.close();
        $scope.isAddingCovariate = false;
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };


  };
  return dependencies.concat(AddCovariateController);
});