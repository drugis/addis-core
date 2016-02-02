'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modal', '$stateParams', '$anchorScroll', '$location',
      'ConceptService', 'VersionedGraphResource', 'CONCEPT_GRAPH_UUID'
    ];
    var ConceptController = function($scope, $modal, $stateParams, $anchorScroll, $location,
      ConceptService, VersionedGraphResource, CONCEPT_GRAPH_UUID) {
      var datasetUri = 'http://trials.drugis/org/datasets/' + $stateParams.datasetUUID;

      function reloadConceptsFromScratch() {
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
              return reloadConceptsFromScratch;
            }
          }
        });
      };

      $scope.datasetConcepts.then(reloadConceptsFromScratch);

      $scope.resetConcepts = function() {
        $scope.$parent.loadConcepts().then(reloadConceptsFromScratch);
      }

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
                $location.path('/users/' + $stateParams.userUid + '/datasets/' + $stateParams.datasetUUID + '/versions/' + newVersion + '/concepts');
              };
            },
            userUid: function() {
              return $stateParams.userUid;
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
