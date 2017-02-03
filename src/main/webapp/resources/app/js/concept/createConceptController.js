'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', 'callback', '$modalInstance', 'ConceptsService', 'concepts'];

  var CreateConceptController = function($scope, $stateParams, callback, $modalInstance, ConceptsService, concepts) {
    $scope.concept = {};
    $scope.isDuplicate = false;

    $scope.typeOptions = [{
      uri: 'ontology:Drug',
      label: 'Drug'
    }, {
      uri: 'ontology:Variable',
      label: 'Variable'
    }, {
      uri: 'ontology:Unit',
      label: 'Unit'
    }];

    $scope.createConcept = function() {
      return ConceptsService.addItem($scope.concept).then(function() {
        callback();
        $modalInstance.close();
      });
    };

    $scope.checkDuplicate = function(newConcept) {
      $scope.isDuplicate = _.find(concepts, function(oldConcept) {
        return newConcept.label === oldConcept.label &&
          (!newConcept.type || newConcept.type.uri === oldConcept.type.uri);
      });
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(CreateConceptController);
});
