'use strict';
define([],
  function() {
    var dependencies = ['$scope', 'MappingService']
    var CreateMappingController = function($scope, MappingService) {
      $scope.createMapping = function() {
        MappingService.addItem($scope.selectedDatasetConcept, $scope.selectedStudyConcept)
      }
    }
    return dependencies.concat(CreateMappingController);
  });