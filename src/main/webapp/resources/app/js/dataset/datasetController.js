'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'DatasetResource'];
    var DatasetController = function($scope, $stateParams, DatasetResource) {
      DatasetResource.get($stateParams).$promise.then(function(result) {
        $scope.dataset = result['@graph'][0];
      });

    };
    return dependencies.concat(DatasetController);
  });
