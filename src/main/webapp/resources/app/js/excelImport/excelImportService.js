'use strict';
define(['lodash', 'util/context', 'xlsx-shim'], function(_, externalContext, XLSX) {
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
    var excelUtils = XLSX.utils;
    var MEASUREMENT_TYPES = [{
        uri: 'ontology:dichotomous',
        label: 'dichotomous'
      },
      {
        uri: 'ontology:continuous',
        label: 'continuous'
      },
      {
        uri: 'ontology:categorical',
        label: 'categorical'
      },
      {
        uri: 'ontology:survival',
        label: 'survival'
      }
    ];

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
      };
      var graph = initialStudyDateSheet(studyDataSheet, uuid);
      var variables = splitVariables(studyDataSheet, graph.has_arm);
      var measurementMoments = getMeasurementMoments(variables);
      graph = addVariables(graph, variables, measurementMoments);
      // graph = addVariables(graph, variables);

      study['@graph'].push(graph);
      return commitStudy(workbook, study, uuid);
    }

    //private
    function getMeasurementMoments(variables) {
      return _.reduce(variables, function(accum, variable) {
        _.forEach(variable.measurementMoments, function(measurementMoment) {
          if (!accum[measurementMoment]) {
            accum[measurementMoment] = INSTANCE_PREFIX + UUIDService.generate();
          }
        });
        return accum;
      }, {});

    }


    function addVariables(graph, variables, measurementMoments) {

      var newGraph = _.cloneDeep(graph);
      _.forEach(variables, function(variable) {
        var newItem;
        if (variable.measurementType === 'ontology:categorical') {
          var bla;
        } else if (variable.measurementType === 'ontology:survival') {
          var bla;
        } else {
          newItem = {
            id: INSTANCE_PREFIX + UUIDService.generate(),
            type: 'ontology:Variable',
            is_measured_at: variable.measurementMoments.length === 1 ? measurementMoments[variable.measurementMoments[0]] : _.map(variable.measurementMoments, function(measurementMoment) {
              return measurementMoments[measurementMoment];
            }),
            label: variable.label,
            has_result_property: [], // todo elk measurement label
            of_variable: [{
              '@type': 'ontology:Variable',
              measurementType: variable.measurementType,
              label: variable.label
            }]
          };
          if (variable.timeScale) {
            newItem.survival_time_scale = variable.timeScale;
          }
        }




        newGraph.has_outcome.push(newItem);
      });
      return newGraph;
    }

    function splitVariables(studyDataSheet, arms) {
      var currentColumn = 12; // =>'M'
      var armsTitlesColumn = 10;
      var variables = [];
      var numberOfVariables = -1;
      var measurementMoment;
      while (studyDataSheet[a1Coordinate(currentColumn, 2)]) { // as long as there is a header
        if (studyDataSheet[a1Coordinate(currentColumn, 2)].v === 'variable type' &&
          studyDataSheet[a1Coordinate(currentColumn, 2)]) {
          // new variable
          ++numberOfVariables;
          variables[numberOfVariables] = {
            label: studyDataSheet[a1Coordinate(currentColumn, 1)].v,
            variableType: studyDataSheet[a1Coordinate(currentColumn, 3)].v,
            measurements: [],
            measurementMoments: []
          };
        } else if (studyDataSheet[a1Coordinate(currentColumn, 2)].v === 'measurement type') {
          variables[numberOfVariables].measurementType = getOntology(MEASUREMENT_TYPES, studyDataSheet[a1Coordinate(currentColumn, 3)].v);
        } else if (studyDataSheet[a1Coordinate(currentColumn, 2)].v === 'measurement moment') {
          measurementMoment = studyDataSheet[a1Coordinate(currentColumn, 3)].v;
          variables[numberOfVariables].measurementMoments.push(measurementMoment);
        } else {
          // measurement
          for (var i = 0; i <= arms.length; ++i) { //for every arm and overall population
            variables[numberOfVariables].measurements.push({
              label: studyDataSheet[a1Coordinate(currentColumn, 2)].v,
              measurementMoment: measurementMoment,
              arms: studyDataSheet[a1Coordinate(armsTitlesColumn, (3 + i))].v,
              value: studyDataSheet[a1Coordinate(currentColumn, (3 + i))] ? studyDataSheet[a1Coordinate(currentColumn, (3 + i))].v : undefined
            });
          }
        }
        ++currentColumn;
      }
      return variables;
    }

    function initialStudyDateSheet(studyDataSheet, uuid) {
      var study = {
        '@id': 'http://trials.drugis.org/studies/' + uuid,
        '@type': 'ontology:Study',
        comment: studyDataSheet.C4.v,
        label: studyDataSheet.A4.v,
        status: studyDataSheet.F4 ? getOntology(STATUS_OPTIONS, studyDataSheet.F4.v) : undefined,
        has_activity: [],
        has_allocation: studyDataSheet.D4 ? getOntology(GROUP_ALLOCATION_OPTIONS, studyDataSheet.D4.v) : undefined,
        has_arm: getArms(studyDataSheet),
        has_blinding: studyDataSheet.E4 ? getOntology(BLINDING_OPTIONS, studyDataSheet.E4.v) : undefined,
        has_eligibility_criteria: getCommented(studyDataSheet, 'J4'),
        // has_epochs: {},
        has_group: [],
        has_included_population: createIncludedPopulation(),
        has_indication: getLabeled(studyDataSheet, 'I4'),
        has_number_of_centers: studyDataSheet.G4 ? studyDataSheet.G4.v : undefined,
        has_objective: getCommented(studyDataSheet, 'H4'),
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

    function getLabeled(studyDataSheet, cell) {
      return studyDataSheet[cell] ? [{
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        label: studyDataSheet[cell].v
      }] : [];
    }

    function getCommented(studyDataSheet, cell) {
      return studyDataSheet[cell] ? [{
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        comment: studyDataSheet[cell].v
      }] : [];
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
      return [newVersionDefer.promise];
    }

    function getOntology(options, inputCell) {
      var result = _.find(options, function(option) {
        return inputCell === option.label;
      });
      return result ? result.uri : undefined;
    }

    function getArms(studyDataSheet) {
      var index = 4;
      var arms = [];
      while (studyDataSheet['K' + index]) {
        arms.push({
          '@id': INSTANCE_PREFIX + UUIDService.generate(),
          label: studyDataSheet['K' + index].v,
          comment: studyDataSheet['L' + index] ? studyDataSheet['L' + index].v : undefined
        });
        ++index;
      }
      _.remove(arms, function(arm) {
        return arm.label === 'Overall population';
      });
      return arms;
    }

    function a1Coordinate(column, row) {
      return excelUtils.encode_cell({
        c: column,
        r: row
      });
    }
    // interface
    return {
      checkWorkbook: checkWorkbook,
      createStudy: createStudy
    };

  };
  return dependencies.concat(ExcelImportService);
});