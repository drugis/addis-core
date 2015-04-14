'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modal', '$stateParams', '$anchorScroll', '$location',
      'ConceptService', 'VersionedGraphResource', 'CONCEPT_GRAPH_UUID'
    ];
    var ConceptController = function($scope, $modal, $stateParams, $anchorScroll, $location,
      ConceptService, VersionedGraphResource, CONCEPT_GRAPH_UUID) {
      var datasetUri = 'http://trials.drugis/org/datasets/' + $stateParams.datasetUUID;

      function reloadConcepts() {
        return ConceptService.queryItems(datasetUri).then(function(conceptsJson) {
          $scope.concepts = conceptsJson;
        });
      }

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

      $scope.datasetConcepts.then(function(result) {
        $scope.concepts = result;
      });

      $scope.areConceptsModified = function() {
        return ConceptService.areConceptsModified();
      };

      $scope.saveConcepts = function() {
        $modal.open({
          templateUrl: 'app/js/commit/commit.html',
          controller: 'CommitController',
          resolve: {
            callback: function() {
              return function(newVersion) {
                ConceptService.conceptsSaved();
                $location.path('/datasets/' + $stateParams.datasetUUID + '/versions/' + newVersion + '/concepts');
              };
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
