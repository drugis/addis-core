'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modal', '$stateParams', '$anchorScroll', '$location',
      'ConceptsService', 'VersionedGraphResource', 'CONCEPT_GRAPH_UUID'
    ];
    var ConceptsController = function($scope, $modal, $stateParams, $anchorScroll, $location,
      ConceptsService, VersionedGraphResource, CONCEPT_GRAPH_UUID) {
      var datasetUri = 'http://trials.drugis/org/datasets/' + $stateParams.datasetUuid;

      function reloadConceptsFromScratch() {
        return ConceptsService.queryItems(datasetUri).then(function(conceptsJson) {
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
            }, concepts: function(){
              return $scope.concepts;
            }
          }
        });
      };

      $scope.datasetConcepts.then(reloadConceptsFromScratch);

      $scope.resetConcepts = function() {
        $scope.$parent.loadConcepts().then(reloadConceptsFromScratch);
      };

      $scope.areConceptsModified = function() {
        return ConceptsService.areConceptsModified();
      };

      $scope.saveConcepts = function() {
        $modal.open({
          templateUrl: 'app/js/commit/commit.html',
          controller: 'CommitController',
          resolve: {
            callback: function() {
              return function(newVersion) {
                ConceptsService.conceptsSaved();
                $location.path('/users/' + $stateParams.userUid + '/datasets/' + $stateParams.datasetUuid + '/versions/' + newVersion + '/concepts');
              };
            },
            userUid: function() {
              return $stateParams.userUid;
            },
            datasetUuid: function() {
              return $stateParams.datasetUuid;
            },
            graphUuid: function() {
              return CONCEPT_GRAPH_UUID;
            },
            itemServiceName: function() {
              return 'ConceptsService';
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
    return dependencies.concat(ConceptsController);
  });
