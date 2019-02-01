'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    'MappingService',
    '$modal'
  ];
  var ConceptMappingItemDirective = function(
    MappingService,
    $modal
  ) {
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
        scope.updateMapping = updateMapping;
        scope.removeMapping = removeMapping;
        scope.openRepairModal = openRepairModal;

        scope.selections = {};
        scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;

        scope.datasetConcepts.then(function(concepts) {
          scope.filteredConcepts = filterConcepts(concepts);
          if (scope.studyConcept.conceptMapping) {
            scope.selectedDatasetConcept = findDatasetConcept(scope.studyConcept.conceptMapping);
          }
          if (scope.selectedDatasetConcept && scope.settings.label === 'Units') {
            scope.selections.selectedMultiplier = findMultiplier();
          }
        });

        function findMultiplier() {
          return _.find(scope.metricMultipliers, function(multiplier) {
            return multiplier.conversionMultiplier === scope.studyConcept.conversionMultiplier;
          });
        }

        function findDatasetConcept(mapping) {
          return _.find(scope.filteredConcepts, function(datasetConcept) {
            return mapping === datasetConcept['@id'];
          });
        }

        function filterConcepts(concepts) {
          return _.filter(concepts['@graph'], function(datasetConcept) {
            return datasetConcept['@type'] === scope.settings.typeUri;
          });
        }

        function updateMapping() {
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
        }

        function removeMapping() {
          MappingService.removeMapping(scope.studyConcept, scope.selectedDatasetConcept);
          scope.selections.selectedMultiplier = undefined;
          scope.selectedDatasetConcept = undefined;
        }

        function openRepairModal() {
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
        }
      }
    };
  };
  return dependencies.concat(ConceptMappingItemDirective);
});
