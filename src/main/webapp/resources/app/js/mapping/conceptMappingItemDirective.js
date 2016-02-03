'use strict';
define([], function() {
  var dependencies = ['MappingService'];
  var ConceptMappingItemDirective = function(MappingService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/mapping/conceptMappingItemDirective.html',
      scope: {
        settings: '=',
        studyConcept: '=',
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
        }

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
