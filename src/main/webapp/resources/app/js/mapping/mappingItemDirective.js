'use strict';
define([], function() {
  var dependencies = ['MappingService'];
  var MappingItemDirective = function(MappingService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/mapping/mappingItemDirective.html',
      scope: {
        studyConcept: '=',
        conceptMapping: '=',
        datasetConcepts: '='
      },
      link: function(scope) {
        scope.updateMapping = function() {
          scope.conceptMapping = MappingService.createMapping(scope.studyConcept, scope.selectedDatasetConcept);
        };

      }
    };
  };
  return dependencies.concat(MappingItemDirective);
});