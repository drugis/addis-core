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
        scope.selections = {};
        scope.metricMultipliers = [{
          label: 'nano',
          conversionMultiplier: 1e-09
        }, {
          label: 'micro',
          conversionMultiplier: 1e-06
        }, {
          label: 'milli',
          conversionMultiplier: 1e-03
        }, {
          label: 'centi',
          conversionMultiplier: 1e-02
        }, {
          label: 'deci',
          conversionMultiplier: 1e-01
        }, {
          label: 'deca',
          conversionMultiplier: 1e01
        }, {
          label: 'hecto',
          conversionMultiplier: 1e02
        }, {
          label: 'kilo',
          conversionMultiplier: 1e03
        }, {
          label: 'mega',
          conversionMultiplier: 1e06
        }];

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
