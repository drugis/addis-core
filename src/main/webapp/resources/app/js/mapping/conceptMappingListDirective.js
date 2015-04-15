'use strict';
define([], function() {
  var dependencies = ['$injector', '$stateParams', 'VersionedGraphResource', 'MappingService'];
  var ConceptMappingListDirective = function($injector, $stateParams, VersionedGraphResource, MappingService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/mapping/conceptMappingListDirective.html',
      scope: {
        settings: '=',
        datasetConcepts: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        var refreshListener;
        var service = $injector.get(scope.settings.serviceName);

        scope.reloadItems = function() {
          if (refreshListener) {
            refreshListener();
          }

          service.queryItems($stateParams.studyUUID).then(function(queryResult) {
            scope.studyConcepts = queryResult;

            refreshListener = scope.$on('refreshStudyDesign', function() {
              scope.reloadItems();
            });
          });
        };

        scope.updateMapping = function(studyConceptIndex, selectedDatasetConcept) {
          MappingService.updateMapping(scope.studyConcepts[studyConceptIndex], selectedDatasetConcept);
        };

        scope.reloadItems();

      }
    };
  };
  return dependencies.concat(ConceptMappingListDirective);
});
