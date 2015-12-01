'use strict';
define([], function() {
  var dependencies = ['$scope', 'CovariateResource', '$modalInstance'];
  var AddCovariateController = function($scope, CovariateResource, $modalInstance) {

    $scope.covariates = [
      {label:'Allocation: Randomized', type:'Study characteristics'},
      {label:'Blinding: at least single blind', type:'Study characteristics'},
      {label:'Blinding: at least double blind', type:'Study characteristics'},
      {label:'Multi-center study', type:'Population characteristics'},
      {label:'Duration of follow-up', type:'Population characteristics'}
    ];

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