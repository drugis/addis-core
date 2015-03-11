'use strict';
define([],
  function() {
    var dependencies=['$scope', 'ConceptService'];
    var ConceptController = function($scope, ConceptService) {
      $scope.concepts = {};
      ConceptService.queryItems($stateParams.datasetUUID).then(function(results) {
        $scope.concepts = results;
      });

    };
    return dependencies.concat(ConceptController);
  });