'use strict';
define([], function() {
  var dependencies = [
    '$injector',
    '$stateParams',
    'MappingService'
  ];
  var ConceptMappingListDirective = function(
    $injector,
    $stateParams,
    MappingService
  ) {
    return {
      restrict: 'E',
      templateUrl: './conceptMappingListDirective.html',
      scope: {
        settings: '=',
        datasetConcepts: '=',
        isEditingAllowed: '='
      },
      link: function(scope) {
        scope.reloadItems = reloadItems;
        scope.updateMapping = updateMapping;

        var refreshListener;
        var service = $injector.get(scope.settings.serviceName);
        scope.reloadItems();

        function reloadItems() {
          if (refreshListener) {
            refreshListener();
          }
          service.queryItems($stateParams.studyGraphUuid).then(function(result) {
            scope.studyConcepts = result;
            refreshListener = scope.$on('refreshStudyDesign', function() {
              scope.reloadItems();
            });
          });
        }

        function updateMapping(studyConceptIndex, selectedDatasetConcept) {
          MappingService.updateMapping(scope.studyConcepts[studyConceptIndex], selectedDatasetConcept);
        }
      }
    };
  };
  return dependencies.concat(ConceptMappingListDirective);
});
