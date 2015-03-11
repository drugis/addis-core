'use strict';
define([],
  function() {
    var dependencies=['$scope', '$stateParams', 'ConceptService'];
    var ConceptController = function($scope, $stateParams, ConceptService) {
      $scope.concepts = {};
      ConceptService.queryItems($stateParams.datasetUUID).then(function(results) {
        $scope.concepts = results;
      });

    };
    return dependencies.concat(ConceptController);
  });