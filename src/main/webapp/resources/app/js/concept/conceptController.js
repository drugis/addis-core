'use strict';
define([],
  function() {
    var dependencies=['$scope', '$stateParams', 'DatasetService', 'DatasetResource', 'ConceptService', 'ConceptResource'];
    var ConceptController = function($scope, $stateParams, DatasetService, DatasetResource, ConceptService, ConceptResource) {
      var datasetUri = 'http://trials.drugis/org/datasets/' + $stateParams.datasetUUID;
      $scope.concepts = {};

      function reloadConceptsModel() {
        ConceptResource.get($stateParams).$promise.then(function(conceptsTurtle) {
          ConceptService.loadStore(conceptsTurtle.data).then(function() {
            ConceptService.queryItems(datasetUri).then(function(conceptsJson) {
              $scope.concepts = conceptsJson;
            });
          });
        });
      }

      function reloadDatasetModel() {
        DatasetResource.get($stateParams, function(response) {
          DatasetService.reset();
          DatasetService.loadStore(response.data).then(function() {
            DatasetService.queryDataset().then(function(queryResult) {
              $scope.dataset = queryResult[0];
              $scope.dataset.uuid = $stateParams.datasetUUID;
            });
          });
        });
      }

      // onload
      reloadDatasetModel();
      reloadConceptsModel();

    };
    return dependencies.concat(ConceptController);
  });