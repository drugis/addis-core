'use strict';
define([],
  function() {
    var dependencies=['$scope', '$stateParams', 'ConceptService', 'ConceptResource'];
    var ConceptController = function($scope, $stateParams, ConceptService, ConceptResource) {
      var datasetUri = 'http://trials.drugis/org/datasets/' + $stateParams.datasetUUID;
      $scope.concepts = {};
      ConceptResource.get($stateParams).$promise.then(function(conceptsTurtle) {
        ConceptService.loadStore(conceptsTurtle.data).then(function() {
          ConceptService.queryItems(datasetUri).then(function(conceptsJson) {
            $scope.concepts = conceptsJson;
          });
        });
      });

    };
    return dependencies.concat(ConceptController);
  });