'use strict';
define([], function() {
  var dependencies = ['$injector', '$stateParams', 'MappingService'];
  var ConceptMappingListDirective = function($injector, $stateParams, MappingService) {
    return {
      restrict: 'E',
      templateUrl: './conceptMappingListDirective.html',
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

          service.queryItems($stateParams.studyGraphUuid).then(function(queryResult) {
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
