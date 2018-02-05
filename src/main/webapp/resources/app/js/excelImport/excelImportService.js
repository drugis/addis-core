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
    var ONTOLOGY_PREFIX = 'http://trials.drugis.org/ontology#';
    var excelUtils = XLSX.utils;
    var MEASUREMENT_TYPES = _.keyBy([{
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
    ], 'label');
    var VARIABLE_TYPES = [{
      uri: 'ontology:PopulationCharacteristic',
      label: 'baseline characteristic'
    }, {
      uri: 'ontology:AdverseEvent',
      label: 'adverse event'
    }, {
      uri: 'ontology:Endpoint',
      label: 'outcome'
    }];

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

    // - create main epoch
    // - create 1 MM for each unique name found, all at end of main epoch
    // - create 1 arm per arm row

    function createStudy(workbook) {
      var studyDataSheet = workbook.Sheets['Study data'];

      var jenaGraph = {
        '@graph': [],
        '@context': externalContext
      };
      var epochs = buildEpochs();
      var studyNode = readStudy(studyDataSheet, epochs);
      var variables = readVariables(studyDataSheet, studyNode.has_arm);
      var measurementMoments = buildMeasurementMoments(variables, epochs);
      variables = measurementMomentLabelToUri(variables, measurementMoments);
      studyNode.has_outcome = variables;
      jenaGraph['@graph'] = addMeasurementMoments(jenaGraph['@graph'], measurementMoments);
      // graph = addVariables(graph, variables);

      jenaGraph['@graph'].push(studyNode);
      return jenaGraph;
    }

    //private
    function buildMeasurementMoments(variables, epochs) {
      var epochUri = epochs[0]['@id'];
      return _(variables)
        .map('is_measured_at')
        .flatten()
        .uniq()
        .map(function(measurementMomentName) {
          return {
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            '@type': 'ontology:MeasurementMoment',
            label: measurementMomentName,
            relative_to_epoch: epochUri,
            relative_to_anchor: 'ontology:anchorEpochEnd',
            time_offset: 'PT0S'
          };
        })
        .value();
    }

    function buildEpochs() {
      return [{
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        '@type': 'ontology:Epoch',
        label: 'Automatically generated primary epoch',
        duration: 'PT0S'
      }];
    }

    function addMeasurementMoments(graph, measurementMoments) {
      var newGraph = _.cloneDeep(graph);
      var measurementMomentNodes = _.map(measurementMoments, function(uri, label) {
        return {
          '@id': uri,
          '@type': 'ontology:MeasurementMoment',
          label: label
        };
      });
      return newGraph.concat(measurementMomentNodes);
    }

    function measurementMomentLabelToUri(variables, measurementMoments) {
      var measurementMomentsByLabel = _.indexBy(measurementMoments, 'label');
      return _.map(variables, function(variable) {
        return _.extend({}, variable, {
          is_measured_at: _.map(variables.is_measured_at, function(measurementLabel) {
            return measurementMomentsByLabel[measurementLabel];
          })
        });
      });
    }

    // function addVariables(study, variables, measurementMoments) {
    //   var newStudy = _.cloneDeep(study);
    //   _.forEach(variables, function(variable) {
    //     var newItem;
    //     if (variable.measurementType === 'ontology:categorical') {
    //       var bla; // FIXME
    //     } else if (variable.measurementType === 'ontology:survival') {
    //       var bla; // FIXME
    //     } else {
    //       newItem = {
    //         '@id': INSTANCE_PREFIX + UUIDService.generate(),
    //         '@type': findOntology(VARIABLE_TYPES, variable.variableType),
    //         is_measured_at: variable.measurementMoments.length === 1 ? measurementMoments[variable.measurementMoments[0]] : _.map(variable.measurementMoments, function(measurementMoment) {
    //           return measurementMoments[measurementMoment];
    //         }),
    //         label: variable.label,
    //         has_result_property: variable.resultProperties, // todo elk measurement label
    //         of_variable: [{
    //           '@type': 'ontology:Variable',
    //           measurementType: variable.measurementType,
    //           label: variable.label
    //         }]
    //       };
    //       if (variable.timeScale) {
    //         newItem.survival_time_scale = variable.timeScale;
    //       }
    //     }
    //     newStudy.has_outcome.push(newItem);
    //   });
    //   return newStudy;
    // }

    function readVariables(studyDataSheet, arms) {
      var startColumn = 12; // =>'M'
      var endColumn = XLSX.utils.decode_range(studyDataSheet['!ref']).e.c;
      var variableColumns = _.filter(_.range(startColumn, endColumn), function(column) {
        return studyDataSheet[a1Coordinate(column, 1)];
      });
      var foo = _.map(variableColumns.slice(0, variableColumns.length - 1), function(n, index) {
        return [n, variableColumns[index + 1]];
      });
      var variables = _.map(foo, _.partial(readVariable, studyDataSheet, arms));
      return variables;
    }

    function readVariable(studyDataSheet, arms, columns) {
      var newVariable = {
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        '@type': findOntology(VARIABLE_TYPES, studyDataSheet[a1Coordinate(columns[0], 3)].v),
        label: studyDataSheet[a1Coordinate(columns[0], 1)].v
      };
      var measurementType = MEASUREMENT_TYPES[studyDataSheet[a1Coordinate(columns[0] + 1, 3)].v].uri;
      var ofVariable = {
        '@type': 'ontology:Variable',
        measurementType: measurementType,
        label: newVariable.label
      };
      if (measurementType === MEASUREMENT_TYPES.categorical.uri) {
        ofVariable.foo = 'bar'; // TODO
      } else {
        newVariable.has_result_property = readResultProperties(studyDataSheet, columns);
      }
      newVariable.of_variable = [ofVariable];
      newVariable.is_measured_at = readMeasurementMoments(studyDataSheet, columns);
      return newVariable;
    }

    function readResultProperties(studyDataSheet, columns) {
      return _(_.range(columns[0] + 2, columns[1]))
        .map(function(column) {
          return studyDataSheet[a1Coordinate(column, 2)].v;
        })
        .reject('measurement moment')
        .uniq()
        .map(function(propertyName) {
          return ONTOLOGY_PREFIX + propertyName;
        })
        .value();
    }

    function readMeasurementMoments(studyDataSheet, columns) {
      return _(_.range(columns[0] + 1, columns[1]))
        .map(function(column) {
          return {
            header: studyDataSheet[a1Coordinate(column, 2)].v,
            value: studyDataSheet[a1Coordinate(column, 3)].v
          };
        })
        .filter(['header', 'measurement moment'])
        .map('value')
        .uniq()
        .value();
    }

    function readStudy(studyDataSheet, epochs) {
      var study = {
        '@id': 'http://trials.drugis.org/studies/' + UUIDService.generate,
        '@type': 'ontology:Study',
        comment: studyDataSheet.C4.v,
        label: studyDataSheet.A4.v,
        status: studyDataSheet.F4 ? findOntology(STATUS_OPTIONS, studyDataSheet.F4.v) : undefined,
        has_activity: [],
        has_allocation: studyDataSheet.D4 ? findOntology(GROUP_ALLOCATION_OPTIONS, studyDataSheet.D4.v) : undefined,
        has_arm: getArms(studyDataSheet),
        has_blinding: studyDataSheet.E4 ? findOntology(BLINDING_OPTIONS, studyDataSheet.E4.v) : undefined,
        has_eligibility_criteria: getCommented(studyDataSheet, 'J4'),
        has_epochs: arrayToRDFList(epochs),
        has_group: [],
        has_included_population: createIncludedPopulation(),
        has_indication: getLabeled(studyDataSheet, 'I4'),
        has_number_of_centers: studyDataSheet.G4 ? studyDataSheet.G4.v : undefined,
        has_objective: getCommented(studyDataSheet, 'H4'),
        has_outcome: [],
        has_primary_epoch: epochs[0]['@id'],
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
        newVersion = newVersion.split('/versions/')[1];
        newVersionDefer.resolve(newVersion);
      }, function(error) {
        console.error('error' + error);
      });
      return [newVersionDefer.promise];
    }

    function arrayToRDFList(array) {
      return array; // FIXME
    }

    function findOntology(options, inputCell) {
      var result = _.find(options, ['label', inputCell]);
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
      _.remove(arms, ['label', 'Overall population']);
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