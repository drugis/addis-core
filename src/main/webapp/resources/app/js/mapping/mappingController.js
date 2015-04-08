'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'MappingService'];

    var MappingController = function($scope, $stateParams, MappingService) {
      $scope.mappings = MappingService.queryItems($stateParams.datasetUUID);
    };
    return dependencies.concat(MappingController);
  });
