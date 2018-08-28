'use strict';
define(['lodash'], function(_) {
  var dependencies = ['MappingService', '$modal'];
  var ConceptMappingItemDirective = function(MappingService, $modal) {
    return {
      restrict: 'E',
      templateUrl: './conceptMappingItemDirective.html',
      scope: {
        settings: '=',
        studyConcept: '=',
        studyConcepts: '=',
        datasetConcepts: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        scope.selections = {};
        scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;

        scope.updateMapping = function() {
          if (scope.selectedDatasetConcept === null) {
            scope.selections.selectedMultiplier = undefined;
            scope.selectedDatasetConcept = undefined;
            return;
          }
          if (scope.selections.selectedMultiplier) {
            scope.studyConcept.conversionMultiplier = scope.selections.selectedMultiplier.conversionMultiplier;
          } else {
            scope.studyConcept.conversionMultiplier = 1e00;
          }
          MappingService.updateMapping(scope.studyConcept, scope.selectedDatasetConcept);
        };

        scope.removeMapping = function() {
          MappingService.removeMapping(scope.studyConcept, scope.selectedDatasetConcept);
          scope.selections.selectedMultiplier = undefined;
          scope.selectedDatasetConcept = undefined;
        };

        scope.openRepairModal = function() {
          $modal.open({
            templateUrl: '../unit/repairUnit.html',
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
          if (scope.selectedDatasetConcept && scope.settings.label === 'Units') {
            scope.selections.selectedMultiplier = _.find(scope.metricMultipliers, function(multiplier) {
              return multiplier.conversionMultiplier === scope.studyConcept.conversionMultiplier;
            });

          }
        });

      }
    };
  };
  return dependencies.concat(ConceptMappingItemDirective);
});
