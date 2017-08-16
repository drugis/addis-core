'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', 'callback', '$modalInstance', 'ConceptsService', 'concepts'];

  var CreateConceptController = function($scope, $stateParams, callback, $modalInstance, ConceptsService, concepts) {
    // init
    $scope.concept = {};
    $scope.isDuplicate = false;
    $scope.typeOptions = ConceptsService.typeOptions;

    // functions
    $scope.checkDuplicate = checkDuplicate;
    $scope.cancel = cancel;
    $scope.createConcept = createConcept;

    function createConcept() {
      return ConceptsService.addItem($scope.concept).then(function() {
        callback();
        $modalInstance.close();
      });
    }

    function checkDuplicate(newConcept) {
      $scope.isDuplicate = _.find(concepts, function(oldConcept) {
        if (oldConcept.label === undefined) {
          return newConcept.label !== undefined && !newConcept.type;
        }
        return newConcept.label !== undefined && newConcept.label.toLowerCase() === oldConcept.label.toLowerCase() &&
          (!newConcept.type || newConcept.type.uri === oldConcept.type.uri);
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  };
  return dependencies.concat(CreateConceptController);
});