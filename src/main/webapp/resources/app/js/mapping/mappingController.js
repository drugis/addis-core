'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', '$modal', 'MappingService', 'DrugService', 'ConceptService'];

    var MappingController = function($scope, $stateParams, $modal, MappingService, DrugService, ConceptService) {

      $scope.datasetConcepts = ConceptService.queryItems().then(function(results) {
        $scope.datasetConcepts = results;
      });

      $scope.drugs = DrugService.queryItems().then(function(result) {
        $scope.drugs = result;
      });

      function reloadMappings() {
        $scope.mappings = MappingService.queryItems($stateParams.datasetUUID);
      }

      $scope.openMappingDialog = function() {
        $modal.open({
          templateUrl: 'app/js/concept/createMapping.html',
          controller: 'CreateMappingController',
          resolve: {
            callback: function() {
              return reloadMappings;
            }
          }
        });
      }

    };
    return dependencies.concat(MappingController);
  });