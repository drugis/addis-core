'use strict';
define(['lodash'], function(_) {
  var dependencies = ['MappingService', '$modal'];
  var ConceptMappingItemDirective = function(MappingService, $modal) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/mapping/conceptMappingItemDirective.html',
      scope: {
        settings: '=',
        studyConcept: '=',
        studyConcepts: '=',
        datasetConcepts: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        scope.updateMapping = function() {
          MappingService.updateMapping(scope.studyConcept, scope.selectedDatasetConcept);
        };

        scope.removeMapping = function() {
          MappingService.removeMapping(scope.studyConcept, scope.selectedDatasetConcept);
          scope.selectedDatasetConcept = undefined;
        };

        scope.openRepairModal = function() {
          $modal.open({
            templateUrl: 'app/js/unit/repairUnit.html',
            scope: scope,
            controller: 'RepairUnitController',
            resolve: {
              unit: function() {
                return scope.studyConcept;
              }
            }
          });
        };

        scope.datasetConcepts.then(function(concepts) {
          scope.filteredConcepts = _.filter(concepts['@graph'], function(datasetConcept) {
            return datasetConcept['@type'] === scope.settings.typeUri;
          });

          if (scope.studyConcept.conceptMapping) {
            scope.selectedDatasetConcept = _.find(scope.filteredConcepts, function(datasetConcept) {
              return scope.studyConcept.conceptMapping === datasetConcept['@id'];
            });
          }
        });

      }
    };
  };
  return dependencies.concat(ConceptMappingItemDirective);
});
