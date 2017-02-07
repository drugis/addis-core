'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$modalInstance', 'callback'];
    var FilterDatasetController = function($scope, $modalInstance, callback) {

      $scope.updateFilteredStudies = function() {
        var filteredStudies = _.filter($scope.studiesWithDetail, function(study) {
          var include = true;
          _.forEach($scope.filter.drugs, function(filter) {
              if (!_.includes(study.drugUris, filter['@id'])) {
                include = false;
              }
          });         
           _.forEach($scope.filter.variables, function(filter) {
              if (!_.includes(study.outcomeUris, filter['@id'])) {
                include = false;
              }
          });
          return include;
        });
        callback($scope.filter.drugs, $scope.filter.variables, filteredStudies);
        $modalInstance.dismiss('cancel');
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(FilterDatasetController);
  });
