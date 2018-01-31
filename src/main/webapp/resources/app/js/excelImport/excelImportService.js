'use strict';
define(['lodash', 'util/context'], function(_, externalContext) {
  var dependencies = [
    '$q',
    '$stateParams',
    'GraphResource',
    'VersionedGraphResource',
    'UUIDService',
    'StudyService',
    'PopulationCharacteristicService',
    'BLINDING_OPTIONS',
    'STATUS_OPTIONS',
    'GROUP_ALLOCATION_OPTIONS'
  ];
  var ExcelImportService = function(
    $q,
    $stateParams,
    GraphResource,
    VersionedGraphResource,
    UUIDService,
    StudyService,
    PopulationCharacteristicService,
    BLINDING_OPTIONS,
    STATUS_OPTIONS,
    GROUP_ALLOCATION_OPTIONS
  ) {
    var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';

    function checkWorkbook(workbook) {
      var errors = [];
      if (!workbook.Sheets['Study data']) {
        errors.push('Study data sheet not found');
        return errors;
      }
      if (!workbook.Sheets['Study data'].A4) {
        errors.push('Short name is missing');
      }
      if (!workbook.Sheets['Study data'].C4) {
        errors.push('Title is missing');
      }
      return errors;
    }

    function createStudy(workbook) {
      var studyDataSheet = workbook.Sheets['Study data'];

      var uuid = UUIDService.generate();
      var study = {
        '@graph': [],
        '@context': externalContext
      }; // = createEmptyStudy(uuid);
      study['@graph'].push(addInitialStudyDateSheet(studyDataSheet, uuid));
      return commitStudy(workbook, study, uuid);
    }

    //private
    function addInitialStudyDateSheet(studyDataSheet, uuid) {
      var study = {
        '@id': 'http://trials.drugis.org/studies/' + uuid,
        '@type': 'ontology:Study',
        comment: studyDataSheet.C4.v,
        label: studyDataSheet.A4.v,
        status: studyDataSheet.F4 ? getOntology(STATUS_OPTIONS, studyDataSheet.F4.v) : undefined,
        has_activity: [],
        has_allocation: studyDataSheet.D4 ? getOntology(GROUP_ALLOCATION_OPTIONS, studyDataSheet.D4.v) : undefined,
        has_arm: [],
        has_blinding: studyDataSheet.E4 ? getOntology(BLINDING_OPTIONS, studyDataSheet.E4.v) : undefined,
        has_eligibility_criteria: [],
        // has_epochs: {},
        has_group: [],
        has_included_population: createIncludedPopulation(),
        has_indication: [],
        has_number_of_centers: studyDataSheet.G4 ? studyDataSheet.G4.v : undefined,
        has_objective: getObjective(studyDataSheet),
        has_outcome: [],
        // has_primary_epoch: undefined,
        has_publication: []
      };
      return study;
    }

    function createIncludedPopulation() {
      return [{
        '@id': 'instance:' + UUIDService.generate(),
        '@type': 'ontology:StudyPopulation'
      }];
    }

    function getObjective(studyDataSheet) {
      return studyDataSheet.H4 ? [{
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        comment: studyDataSheet.H4.v
      }] : undefined;
    }

    function commitStudy(workbook, study, uuid) {
      var newVersionDefer = $q.defer();
      GraphResource.putJson({
        userUid: $stateParams.userUid,
        datasetUuid: $stateParams.datasetUuid,
        graphUuid: uuid,
        commitTitle: 'Initial study creation: ' + study['@graph'][0].label
      }, study, function(value, responseHeaders) {
        var newVersion = responseHeaders('X-EventSource-Version');
        newVersion = newVersion.split('/')[4];
        newVersionDefer.resolve(newVersion);
      }, function(error) {
        console.error('error' + error);
      });
      var getStudyFromBackendDefer = $q.defer();
      var allAddedPromise = newVersionDefer.promise.then(function(studyGraphUuid) {
        getStudyFromBackendDefer = GraphResource.getJson({
          userUid: $stateParams.userUid,
          datasetUuid: $stateParams.datasetUuid,
          graphUuid: studyGraphUuid
        });
        StudyService.loadJson(getStudyFromBackendDefer.$promise);
        var studyDataSheet = workbook.Sheets['Study data'];
				PopulationCharacteristicService.addItem({
          indication: {
            label: studyDataSheet.I4 ? studyDataSheet.I4.v : undefined
          },
          eligibilityCriteria: {
            label: studyDataSheet.J4 ? studyDataSheet.J4.v : undefined
          }
        });
      });
      console.log('something');
      return [newVersionDefer.promise, getStudyFromBackendDefer.promise];
    }

    function getOntology(options, inputCell) {
      var result = _.find(options, function(option) {
        return inputCell === option.label;
      });
      return result ? result.uri : undefined;
    }

    // interface
    return {
      checkWorkbook: checkWorkbook,
      createStudy: createStudy
    };

  };
  return dependencies.concat(ExcelImportService);
});