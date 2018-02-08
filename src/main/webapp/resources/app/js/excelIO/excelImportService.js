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
    'RdfListService',
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
    RdfListService,
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
    var EPOCH_ANCHOR = _.keyBy([{
      uri: 'ontology:anchorEpochEnd',
      label: 'end'
    }, {
      uri: 'ontology:anchorEpochStart',
      label: 'start'
    }], 'label');

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
      var lastRow = excelUtils.decode_range(workbook.Sheets['Study data']['!ref']).e.r + 1;
      if (workbook.Sheets['Study data']['K' + lastRow].v !== 'Overall population') {
        errors.push('No overall population found');
      }
      var sheetError = checkSheets(workbook);
      if (sheetError) {
        errors.push(sheetError);
      }
      return errors;
    }

    function createStudy(workbook) {
      var studyDataSheet = workbook.Sheets['Study data'];
      var measurementMoments;
      var epochs = buildEpochs(workbook.Sheets.Epochs);
      var concepts = buildConcepts(workbook.Sheets.Concepts);
      var activities = buildActivities(workbook.Sheets.Activities, workbook);
      if (!checkSheets(workbook)) {
        var measurementMomentSheet = workbook.Sheets['Measurement moments'];
        measurementMoments = buildMultiSheetMeasurementMoments(measurementMomentSheet, workbook);
      } else{
        measurementMoments = buildSingleSheetMeasurementMoments(variables, epochs);
      }

      var studyNode = readStudy(studyDataSheet, epochs);
      var variableColumns = findVariableStartColumns(studyDataSheet);
      var variables = readVariables(studyDataSheet, variableColumns);
      variables = measurementMomentLabelToUri(variables, measurementMoments);
      var measurements = readMeasurements(studyDataSheet, variables, studyNode.has_arm.concat(studyNode.has_included_population), variableColumns);
      studyNode.has_outcome = variables;
      var jenaGraph = {
        '@graph': [].concat(measurementMoments, measurements, studyNode),
        '@context': externalContext
      };
      return jenaGraph;
    }

    //private
    function checkSheets(workbook) {
      var allSheetNames = ['Study data', 'Activities', 'Epochs', 'Study design', 'Measurement moments', 'Concepts'];
      var sheetNames = _.keys(workbook.Sheets);
      if (_.difference(sheetNames, ['Study data']).length !== 0 && _.difference(sheetNames, allSheetNames).length !== 0) {
        return 'Excel file should either only have a Study data worksheet, or all required worksheets.';
      }
    }

    function buildActivities(activitySheet, workbook){
      var lastRow = excelUtils.decode_range(activitySheet['!ref']).e.r;
      return _.map(_.range(1, lastRow + 2), _.partial(readActivity, activitySheet, workbook)); // +2 because zero-indexed and range has open upper end
      
    }

    function readActivity(activitySheet, workbook, row){
      var activityLabelToUri  = {
        'drug treatment': 'ontology:TreatmentActivity'
      };
      var basicActivity =  {activityUri: getValueIfPresent(activitySheet, 0, row),
        label: getValueIfPresent(activitySheet, 1, row),
        activityType: {
          label: getValueIfPresent(activitySheet, 2, row),
          uri: activityLabelToUri[getValueIfPresent(activitySheet, 2, row)]
        },
        activityDescription: getValueIfPresent(activitySheet, 3, row)        
      };
      var hasDrug = getReferenceValue(activitySheet,4,row, workbook);
      if(!hasDrug)     {
        return basicActivity;
      } else{
        return _.merge({},basicActivity, {});
      }

    }

    function buildConcepts(conceptSheet){
      var lastRow = excelUtils.decode_range(conceptSheet['!ref']).e.r;
      return _.map(_.range(1, lastRow + 2), _.partial(readConcept, conceptSheet)); // +2 because zero-indexed and range has open upper end
    }

    function readConcept(conceptSheet, row){
      return {
        uri: getValueIfPresent(conceptSheet, 0, row),
        label: getValueIfPresent(conceptSheet, 1, row),
        //type: getValueIfPresent(conceptSheet, 2, row),
        conceptMapping: getValueIfPresent(conceptSheet, 3, row),
        conversionMultiplier: getValueIfPresent(conceptSheet, 4, row)
      };
    }

    function findVariableStartColumns(studyDataSheet) {
      var startColumn = 12; // =>'M'
      var endColumn = XLSX.utils.decode_range(studyDataSheet['!ref']).e.c;
      return _.filter(_.range(startColumn, endColumn), function(column) {
        return getValueIfPresent(studyDataSheet, column, 1);
      });

    }

    function buildMultiSheetMeasurementMoments(measurementMomentSheet, workbook) {
      var lastRow = excelUtils.decode_range(measurementMomentSheet['!ref']).e.r;
      return _.map(_.range(1, lastRow + 2), _.partial(readMeasurementMoment, measurementMomentSheet, workbook)); // +2 because zero-indexed and range has open upper end
    }

    function readMeasurementMoment(measurementMomentSheet, workbook, row) {
      return {
        '@id': getValueIfPresent(measurementMomentSheet, 0, row),
        '@type': 'ontology:MeasurementMoment',
        label: getValueIfPresent(measurementMomentSheet, 1, row),
        relative_to_epoch: getReferenceValue(measurementMomentSheet, 2, row, workbook),
        relative_to_anchor: EPOCH_ANCHOR[getValueIfPresent(measurementMomentSheet, 3, row)],
        time_offset: getValueIfPresent(measurementMomentSheet, 4, row)
      };
    }

    function buildSingleSheetMeasurementMoments(variables, epochs) {
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

    function buildEpochs(epochSheet) {
      if (!epochSheet) {
        return [{
          '@id': INSTANCE_PREFIX + UUIDService.generate(),
          '@type': 'ontology:Epoch',
          label: 'Automatically generated primary epoch',
          duration: 'PT0S'
        }];
      } else {
        var lastRow = excelUtils.decode_range(epochSheet['!ref']).e.r;
        return _.map(_.range(1, lastRow + 2), _.partial(readEpoch, epochSheet)); // +2 because zero-indexed and range has open upper end
      }
    }

    function readEpoch(epochSheet, row) {
      return {
        '@id': getValueIfPresent(epochSheet, 0, row),
        '@type': 'ontology:Epoch',
        label: getValueIfPresent(epochSheet, 1, row),
        comment: getValueIfPresent(epochSheet, 2, row),
        duration: getValueIfPresent(epochSheet, 3, row),
      };
    }


    function measurementMomentLabelToUri(variables, measurementMoments) {
      var measurementMomentsByLabel = _.keyBy(measurementMoments, 'label');
      return _.map(variables, function(variable) {
        var measurementMomentUris;
        if (variable.is_measured_at.length === 1) {
          measurementMomentUris = measurementMomentsByLabel[variable.is_measured_at]['@id'];
        } else {
          measurementMomentUris = _.map(variable.is_measured_at, function(measurementLabel) {
            return measurementMomentsByLabel[measurementLabel]['@id'];
          });
        }
        return _.extend({}, variable, {
          is_measured_at: measurementMomentUris
        });
      });
    }

    function readVariables(studyDataSheet, variableColumns) {
      var endColumn = XLSX.utils.decode_range(studyDataSheet['!ref']).e.c + 1;
      var variableColumnsPlusEnd = variableColumns.concat(endColumn); // add end column for last variable
      var variableColumnBoundaries = _.map(variableColumnsPlusEnd.slice(0, variableColumnsPlusEnd.length - 1), function(n, index) {
        return [n, variableColumnsPlusEnd[index + 1]];
      });
      var variables = _.map(variableColumnBoundaries, _.partial(readVariable, studyDataSheet));
      return variables;
    }

    function readVariable(studyDataSheet, columns) {
      var newVariable = {
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        '@type': findOntology(VARIABLE_TYPES, getValueIfPresent(studyDataSheet, columns[0], 3)),
        label: getValueIfPresent(studyDataSheet, columns[0], 1)
      };
      var measurementType = MEASUREMENT_TYPES[getValueIfPresent(studyDataSheet, columns[0] + 1, 3)].uri;
      var ofVariable = {
        '@type': 'ontology:Variable',
        measurementType: measurementType,
        label: newVariable.label
      };
      if (measurementType === MEASUREMENT_TYPES.categorical.uri) {
        var categoryNames = readDataColumnNames(studyDataSheet, columns, _.identity);
        var categories = _.map(categoryNames, function(categoryName) {
          return {
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            '@type': 'ontology:Category',
            label: categoryName
          };
        });
        ofVariable.categoryList = RdfListService.unFlattenList(categories);
      } else {
        newVariable.has_result_property = readDataColumnNames(studyDataSheet, columns, function(propertyName) {
          return ONTOLOGY_PREFIX + propertyName;
        });
      }
      newVariable.of_variable = [ofVariable];
      newVariable.is_measured_at = readMeasurementMoments(studyDataSheet, columns);
      return newVariable;
    }

    function readDataColumnNames(studyDataSheet, columns, prefixer) {
      return _(_.range(columns[0] + 2, columns[1]))
        .map(function(column) {
          return getValueIfPresent(studyDataSheet, column, 2);
        })
        .without('measurement moment')
        .uniq()
        .map(prefixer)
        .value();
    }

    function readMeasurementMoments(studyDataSheet, columns) {
      return _(_.range(columns[0] + 1, columns[1]))
        .map(function(column) {
          return {
            column: column,
            header: getValueIfPresent(studyDataSheet, column, 2),
            value: getValueIfPresent(studyDataSheet, column, 3)
          };
        })
        .filter(['header', 'measurement moment'])
        .map('value')
        .uniq()
        .value();
    }

    function readMeasurements(studyDataSheet, variables, arms, columns) {
      var measurements = [];
      _.forEach(variables, function(variable, variableIndex) {
        var variableColumn = columns[variableIndex];
        _.forEach(arms, function(arm, armIndex) {
          var currentY = armIndex + 3;
          var measuredAt = variable.is_measured_at;
          if (!Array.isArray(variable.is_measured_at)) {
            measuredAt = [variable.is_measured_at];
          }
          _.forEach(measuredAt, function(measurementMomentUri, measurementMomentIndex) {
            var measurement = buildMeasurement(studyDataSheet, variable, measurementMomentUri, measurementMomentIndex, arm, variableColumn, currentY);
            if (_.keys(measurement).length > 4) {
              measurements.push(measurement);
            }
          });
        });
      });
      return measurements;
    }

    function buildMeasurement(studyDataSheet, variable, measurementMomentUri, measurementMomentIndex, arm, variableColumn, currentY) {
      var measurement = {
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        of_moment: measurementMomentUri,
        of_group: arm['@id'],
        of_outcome: variable['@id']
      };
      var resultColumns, readMeasurementValue;
      if (variable.of_variable[0].measurementType === MEASUREMENT_TYPES.categorical.uri) {
        resultColumns = RdfListService.flattenList(variable.of_variable[0].categoryList);
        readMeasurementValue = function(measurement, resultColumn, value) {
          var newMeasurement = _.cloneDeep(measurement);
          if (!newMeasurement.category_count) {
            newMeasurement.category_count = [];
          }
          newMeasurement.category_count.push({
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            category: resultColumn['@id'],
            count: value
          });
          return newMeasurement;
        };
      } else {
        resultColumns = _.map(variable.has_result_property, function(resultProperty) {
          var splitProperty = resultProperty.split('#');
          return splitProperty[1];
        });
        readMeasurementValue = function(measurement, resultColumn, value) {
          var newMeasurement = _.cloneDeep(measurement);
          newMeasurement[resultColumn] = value;
          return newMeasurement;
        };
      }

      _.forEach(resultColumns, function(resultColumn, propertyIndex) {
        var currentX = variableColumn + 3 + measurementMomentIndex * (resultColumns.length + 1) + propertyIndex;
        var currentValue = getValueIfPresent(studyDataSheet, currentX, currentY);
        if (currentValue !== undefined && currentValue !== null) {
          measurement = readMeasurementValue(measurement, resultColumn, currentValue);
        }
      });
      return measurement;
    }

    function readStudy(studyDataSheet, epochs) {
      var study = {
        '@id': 'http://trials.drugis.org/studies/' + UUIDService.generate(),
        '@type': 'ontology:Study',
        comment: studyDataSheet.C4.v,
        label: studyDataSheet.A4.v,
        status: studyDataSheet.F4 ? findOntology(STATUS_OPTIONS, studyDataSheet.F4.v) : undefined,
        has_activity: [],
        has_allocation: studyDataSheet.D4 ? findOntology(GROUP_ALLOCATION_OPTIONS, studyDataSheet.D4.v) : undefined,
        has_arm: readArms(studyDataSheet),
        has_blinding: studyDataSheet.E4 ? findOntology(BLINDING_OPTIONS, studyDataSheet.E4.v) : undefined,
        has_eligibility_criteria: getCommented(studyDataSheet, 'J4'),
        has_epochs: RdfListService.unFlattenList(epochs),
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
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        '@type': 'ontology:StudyPopulation'
      }];
    }

    // FIXME: refactor
    function getLabeled(studyDataSheet, cell) {
      return studyDataSheet[cell] ? [{
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        label: studyDataSheet[cell].v
      }] : [];
    }

    // FIXME: refactor
    function getCommented(studyDataSheet, cell) {
      return studyDataSheet[cell] ? [{
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        comment: studyDataSheet[cell].v
      }] : [];
    }

    function commitStudy(study) {
      var newVersionDefer = $q.defer();
      GraphResource.putJson({
        userUid: $stateParams.userUid,
        datasetUuid: $stateParams.datasetUuid,
        graphUuid: UUIDService.generate(),
        commitTitle: 'Initial study creation: ' + study['@graph'][0].label
      }, study, function(value, responseHeaders) {
        var newVersion = responseHeaders('X-EventSource-Version');
        newVersion = newVersion.split('/versions/')[1];
        newVersionDefer.resolve(newVersion);
      }, function(error) {
        console.error('error' + error);
      });
      return newVersionDefer.promise;
    }

    function findOntology(options, inputCell) {
      var result = _.find(options, ['label', inputCell]);
      return result ? result.uri : undefined;
    }

    function readArms(studyDataSheet) {
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

    function getReferenceValue(sourceSheet, column, row, workbook) {
      var source = sourceSheet[a1Coordinate(column, row)];
      if (source) {
        var splittedFormula = source.f.split('!');
        var targetSheet = splittedFormula[0];
        if (targetSheet[0] === '=') {
          targetSheet = targetSheet.split('=')[1];
        }
        var targetCell = splittedFormula[1];
        if (workbook[targetSheet] && workbook[targetSheet][targetCell]) {
          return workbook[targetSheet][targetCell].v;
        } else {
          throw 'Broken reference: ' + source.f;
        }
      }
    }

    function getValueIfPresent(dataSheet, column, row) {
      var cell = dataSheet[a1Coordinate(column, row)];
      return cell ? cell.v : undefined;
    }

    // interface
    return {
      checkWorkbook: checkWorkbook,
      createStudy: createStudy,
      commitStudy: commitStudy
    };

  };
  return dependencies.concat(ExcelImportService);
});