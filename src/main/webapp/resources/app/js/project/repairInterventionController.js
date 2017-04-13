'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', '$q',
    'InterventionResource', 'intervention', 'callback',
    'ConceptsService', 'DataModelService', 'VersionedGraphResource', 'GraphResource'
  ];
  var RepairInterventionController = function($scope, $stateParams, $modalInstance, $q,
    InterventionResource, intervention, callback,
    ConceptsService, DataModelService, VersionedGraphResource, GraphResource) {
    var datasetUri = 'http://trials.drugis/org/datasets/' + $scope.project.namespaceUid;
    $scope.intervention = intervention;
    $scope.updateInterventionMultiplierMapping = updateInterventionMultiplierMapping;
    $scope.units = [];
    $scope.metricMultipliers = [{
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
      label: 'no multiplier',
      conversionMultiplier: 1e00
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
    loadConcepts().then(function() {
      buildUnits();
    });

    function loadConcepts() {
      var conceptsPromise;
      if ($scope.project.datasetVersion) {
        conceptsPromise = VersionedGraphResource.getConceptJson({
          userUid: $scope.currentRevision.userId,
          datasetUuid: $scope.project.namespaceUid,
          graphUuid: 'concepts',
          versionUuid: $scope.currentRevision.uri.split('/versions/')[1]
        }).$promise;
      } else {
        conceptsPromise = GraphResource.getConceptJson({
          userUid: $scope.currentRevision.userId,
          datasetUuid: $scope.project.namespaceUid,
          graphUuid: 'concepts',
        }).$promise;
      }
      var cleanedConceptsPromise = conceptsPromise.then(function(conceptsData) {
        return DataModelService.correctUnitConceptType(conceptsData);
      });
      ConceptsService.loadJson(cleanedConceptsPromise);
      return ConceptsService.queryItems(datasetUri).then(function(conceptsJson) {
        $scope.concepts = conceptsJson;
      });
    }



    function buildUnits() {
      if ($scope.intervention.type === 'fixed') {
        if ($scope.intervention.constraint.lowerBound) {
          $scope.units.push({
            unitName: $scope.intervention.constraint.lowerBound.unitName,
            unitConcept: $scope.intervention.constraint.lowerBound.unitConcept,
            conversionMultiplier: $scope.intervention.constraint.lowerBound.conversionMultiplier
          });
        }
        if ($scope.intervention.constraint.upperBound) {
          $scope.units.push({
            unitName: $scope.intervention.constraint.upperBound.unitName,
            unitConcept: $scope.intervention.constraint.upperBound.unitConcept,
            conversionMultiplier: $scope.intervention.constraint.upperBound.conversionMultiplier
          });
        }
      } else if ($scope.intervention.type === 'titrated') {
        //min
        if ($scope.intervention.maxConstraint.lowerBound) {
          $scope.units.push({
            unitName: $scope.intervention.minConstraint.lowerBound.unitName,
            unitConcept: $scope.intervention.minConstraint.lowerBound.unitConcept,
            conversionMultiplier: $scope.intervention.minConstraint.lowerBound.conversionMultiplier
          });
        }
        if ($scope.intervention.minConstraint.upperBound) {
          $scope.units.push({
            unitName: $scope.intervention.minConstraint.upperBound.unitName,
            unitConcept: $scope.intervention.minConstraint.upperBound.unitConcept,
            conversionMultiplier: $scope.intervention.minConstraint.upperBound.conversionMultiplier
          });
        }
        //max
        if ($scope.intervention.maxConstraint.upperBound) {
          $scope.units.push({
            unitName: $scope.intervention.maxConstraint.lowerBound.unitName,
            unitConcept: $scope.intervention.maxConstraint.lowerBound.unitConcept,
            conversionMultiplier: $scope.intervention.maxConstraint.lowerBound.conversionMultiplier
          });
        }
        if ($scope.intervention.maxConstraint.upperBound) {
          $scope.units.push({
            unitName: $scope.intervention.maxConstraint.upperBound.unitName,
            unitConcept: $scope.intervention.maxConstraint.upperBound.unitConcept,
            conversionMultiplier: $scope.intervention.maxConstraint.upperBound.conversionMultiplier
          });
        }

      }
      $scope.units = _.uniqBy($scope.units, ['unitName', 'unitConcept']);
      _.forEach($scope.units, function(unit) {
        var concept = _.find($scope.concepts, function(concept) {
          return concept.uri === unit.unitConcept;
        });
        unit.name = concept.label;
      });
    }

    function updateInterventionMultiplierMapping() {
      var units = _.map($scope.units, function(unit) {
        delete unit.name;
        return unit;
      });
      InterventionResource.setConversionMultiplier({
        projectId: $scope.project.id,
        interventionId: $scope.intervention.id
      }, {
        multipliers: units
      }).$promise.then(function() {
        callback();
        $modalInstance.close();
      });
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(RepairInterventionController);
});
