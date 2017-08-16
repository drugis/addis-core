'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', '$q',
    'InterventionResource', 'intervention', 'callback',
    'ConceptsService', 'MappingService', 'DataModelService', 'VersionedGraphResource'
  ];
  var RepairInterventionController = function($scope, $stateParams, $modalInstance, $q,
    InterventionResource, intervention, callback,
    ConceptsService, MappingService, DataModelService, VersionedGraphResource) {
    // functions
    $scope.updateInterventionMultiplierMapping = updateInterventionMultiplierMapping;
    $scope.cancel = cancel;

    // init
    var datasetUri = 'http://trials.drugis/org/datasets/' + $scope.project.namespaceUid;
    $scope.intervention = intervention;
    $scope.units = [];
    $scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;

    loadConcepts();

    function loadConcepts() {
      var conceptsPromise;
      conceptsPromise = VersionedGraphResource.getConceptJson({
        userUid: $scope.currentRevision.userId,
        datasetUuid: $scope.project.namespaceUid,
        graphUuid: 'concepts',
        versionUuid: $scope.currentRevision.uri.split('/versions/')[1]
      }).$promise;
      var cleanedConceptsPromise = conceptsPromise.then(function(conceptsData) {
        return DataModelService.correctUnitConceptType(conceptsData);
      });
      ConceptsService.loadJson(cleanedConceptsPromise);
      ConceptsService.queryItems(datasetUri)
        .then(function(conceptsJson) {
          $scope.conceptsByUri = _.keyBy(conceptsJson, 'uri');
        }).then(function() {
          $scope.units = MappingService.getUnitsFromIntervention($scope.intervention, $scope.concepts);
        });
    }

    function updateInterventionMultiplierMapping() {
      InterventionResource.setConversionMultiplier({
        projectId: $scope.project.id,
        interventionId: $scope.intervention.id
      }, {
        multipliers: $scope.units
      }).$promise.then(function() {
        callback();
        $modalInstance.close();
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  };
  return dependencies.concat(RepairInterventionController);
});