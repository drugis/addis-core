'use strict';
define([], function() {
  var dependencies = ['$injector', '$stateParams', 'VersionedGraphResource', 'ConceptService', 'MappingService'];
  var ConceptMappingListDirective = function($injector, $stateParams, VersionedGraphResource, ConceptService, MappingService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/study/conceptMappingListDirective.html',
      scope: {
        settings: '='
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

        // load concepts
        VersionedGraphResource.get({
          datasetUUID: $stateParams.datasetUUID,
          graphUuid: 'concepts',
          versionUuid: $stateParams.versionUuid
        }).$promise.then(function(conceptsTurtle) {
          ConceptService.loadStore(conceptsTurtle.data).then(function() {
            ConceptService.queryItems($stateParams.datasetUUID).then(function(result) {
              scope.datasetConcepts = _.filter(result, function(datasetConcept){
                return datasetConcept.type === scope.settings.typeUri;
              });
            });
          });
        });

        scope.reloadItems();

        scope.updateMapping = function() {
          scope.conceptMapping = MappingService.createMapping(scope.studyConcept, scope.selectedDatasetConcept);
        };
      }
    };
  };
  return dependencies.concat(ConceptMappingListDirective);
});