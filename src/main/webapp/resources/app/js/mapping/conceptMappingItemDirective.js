'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$rootScope',
    '$modal',
    'MappingService'
  ];
  var ConceptMappingItemDirective = function(
    $rootScope,
    $modal,
    MappingService
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
          checkDoubleMapping(undefined, {});
        });

        $rootScope.$on('doubleMapping', checkDoubleMapping);

        function checkDoubleMapping(event, data) {
          if (!scope.selectedDatasetConcept || scope.studyConcept.uri === data.uri) {
            return;
          } else {
            MappingService.hasDoubleMapping(scope.studyConcept, scope.selectedDatasetConcept).then(function(result) {
              scope.doubleMapping = result;
            });
          }
        }

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

        function updateMapping(oldType) {
          if (scope.selectedDatasetConcept === null) {
            removeMapping({
              '@type': oldType
            });
            return;
          }
          if (scope.selections.selectedMultiplier) {
            scope.studyConcept.conversionMultiplier = scope.selections.selectedMultiplier.conversionMultiplier;
          } else {
            scope.studyConcept.conversionMultiplier = 1e00;
          }
          MappingService.updateMapping(scope.studyConcept, scope.selectedDatasetConcept).then(function() {
            MappingService.hasDoubleMapping(scope.studyConcept, scope.selectedDatasetConcept).then(function(result) {
              scope.doubleMapping = result;
              $rootScope.$broadcast('doubleMapping', scope.studyConcept);
            });
          });
        }

        function removeMapping(selectedDatasetConcept) {
          MappingService.removeMapping(scope.studyConcept, selectedDatasetConcept);
          scope.selections.selectedMultiplier = undefined;
          scope.selectedDatasetConcept = undefined;
          scope.doubleMapping = false;
          $rootScope.$broadcast('doubleMapping', scope.studyConcept);
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
