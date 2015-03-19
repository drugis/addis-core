'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modal', '$stateParams', 'DatasetService', 'DatasetResource', 'ConceptService', 'GraphResource'];
    var ConceptController = function($scope, $modal, $stateParams, DatasetService, DatasetResource, ConceptService, GraphResource) {
      var datasetUri = 'http://trials.drugis/org/datasets/' + $stateParams.datasetUUID;
      $scope.concepts = {};

      function reloadConceptsModel() {
        GraphResource.get({
          datasetUUID: $stateParams.datasetUUID,
          graphUuid: 'concepts'
        }).$promise.then(function(conceptsTurtle) {
          ConceptService.loadStore(conceptsTurtle.data).then(reloadConcepts);
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

      function reloadConcepts() {
        return ConceptService.queryItems(datasetUri).then(function(conceptsJson) {
          $scope.concepts = conceptsJson;
        });
      }

      // onload
      reloadDatasetModel();
      reloadConceptsModel();

      $scope.openAddConceptDialog = function() {
        $modal.open({
          templateUrl: 'app/js/concept/createConcept.html',
          controller: 'CreateConceptController',
          resolve: {
            callback: function() {
              return reloadConcepts;
            }
          }
        });
      };

      $scope.areConceptsModified = function() {
        return ConceptService.areConceptsModified();
      }

      $scope.saveConcepts = function() {
        ConceptService.getGraph().then(function(graph) {
          GraphResource.put({
            datasetUUID: $stateParams.datasetUUID,
            graphUuid: 'concepts'
          }, graph.data, function() {
            console.log('graph saved');
            ConceptService.conceptsSaved();
          });
        });
      }

    };
    return dependencies.concat(ConceptController);
  });
