'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modal', '$stateParams', '$anchorScroll', '$location',
      'ConceptService', 'VersionedGraphResource', 'CONCEPT_GRAPH_UUID'
    ];
    var ConceptController = function($scope, $modal, $stateParams, $anchorScroll, $location,
      ConceptService, VersionedGraphResource, CONCEPT_GRAPH_UUID) {
      var datasetUri = 'http://trials.drugis/org/datasets/' + $stateParams.datasetUUID;
      $scope.concepts = {};

      function reloadConceptsModel() {
        VersionedGraphResource.get({
          datasetUUID: $stateParams.datasetUUID,
          graphUuid: 'concepts',
          versionUuid: $stateParams.versionUuid
        }).$promise.then(function(conceptsTurtle) {
          ConceptService.loadStore(conceptsTurtle.data).then(reloadConcepts);
        });
      }

      function reloadConcepts() {
        return ConceptService.queryItems(datasetUri).then(function(conceptsJson) {
          $scope.concepts = conceptsJson;
        });
      }

      $scope.resetConcepts = function() { 
        reloadConceptsModel();
      };

      // onload
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
      };

      $scope.saveConcepts = function() {
        $modal.open({
          templateUrl: 'app/js/commit/commit.html',
          controller: 'CommitController',
          resolve: {
            callback: function() {
              return ConceptService.conceptsSaved;
            },
            datasetUuid: function() {
              return $stateParams.datasetUUID;
            },
            graphUuid: function() {
              return CONCEPT_GRAPH_UUID;
            },
            itemServiceName: function() {
              return 'ConceptService';
            }
          }
        });
      };

      $scope.sideNavClick = function(anchor) {
        var newHash = anchor;
        $anchorScroll.yOffset = 73;
        if ($location.hash() !== newHash) {
          $location.hash(anchor);
        } else {
          $anchorScroll();
        }
      };

    };
    return dependencies.concat(ConceptController);
  });
