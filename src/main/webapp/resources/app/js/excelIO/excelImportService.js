'use strict';
define(['lodash', '../util/context', 'xlsx'], function(_, externalContext, XLSX) {
  var dependencies = [
    '$q',
    '$stateParams',
    '$timeout',
    'GraphResource',
    'VersionedGraphResource',
    'UUIDService',
    'StudyService',
    'PopulationCharacteristicService',
    'RdfListService',
    'ExcelIOUtilService',
    'ACTIVITY_TYPE_OPTIONS',
    'BLINDING_OPTIONS',
    'STATUS_OPTIONS',
    'GROUP_ALLOCATION_OPTIONS'
  ];
  var ExcelImportService = function(
    $q,
    $stateParams,
    $timeout,
    GraphResource,
    VersionedGraphResource,
    UUIDService,
    StudyService,
    PopulationCharacteristicService,
    RdfListService,
    IOU,
    ACTIVITY_TYPE_OPTIONS,
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
    var STUDY_SHEET_NAMES = ['Study data', 'Activities', 'Epochs', 'Study design', 'Measurement moments', 'Concepts'];

    function checkSingleStudyWorkbook(workbook) {
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

    function checkDatasetWorkbook(workbook) {
      var errors = [];
      var sheetError = checkDatasetSheets(workbook);
      if (sheetError) {
        errors.push(sheetError);
      }
      if (!workbook.Sheets['Dataset information'].A2) {
        errors.push('No dataset title found');
      }
      return errors;
    }

    function createStudy(workbook) {
      if (workbook.Sheets.Concepts) { //At this point we already know the workbook either has all sheets, or only study data
        var startRows = {
          'Study data': 1,
          'Activities': 0,
          'Epochs': 0,
          'Study design': 0,
          'Measurement moments': 0,
          'Concepts': 0
        };
        return createStructuredStudy(workbook, startRows);
      } else {
        return createSimpleStudy(workbook);
      }
    }

    function createStructuredStudy(workbook, startRows) {
      var studyDataSheet = workbook.Sheets['Study data'];
      var measurementMomentSheet = workbook.Sheets['Measurement moments'];
      var epochs = buildEpochs(workbook.Sheets.Epochs, startRows.Epochs);
      var primaryEpoch = _.find(epochs, ['isPrimary', 'true']);
      epochs = _(epochs).map(_.partialRight(_.omit, 'isPrimary')).value();
      var measurementMoments = buildStructuredMeasurementMoments(measurementMomentSheet, workbook, startRows['Measurement moments']);

      var studyNode = readStudy(studyDataSheet, epochs, startRows['Study data']);
      studyNode.has_primary_epoch = primaryEpoch ? primaryEpoch['@id'] : undefined;
      studyNode.has_activity = buildActivities(workbook.Sheets.Activities, workbook, startRows.Activities);
      var variableColumns = findVariableStartColumns(studyDataSheet, startRows['Study data']);
      var variables = readVariables(studyDataSheet, variableColumns, workbook, startRows['Study data']);
      var measurements = readMeasurements(studyDataSheet, variables,
        studyNode.has_arm.concat(studyNode.has_included_population), variableColumns, startRows['Study data']);
      studyNode.has_outcome = variables;
      studyNode.has_activity = addStudyDesign(studyNode, workbook, startRows['Study design']);
      var drugsAndUnits = buildDrugsAndUnits(workbook.Sheets.Concepts, workbook, startRows.Concepts);
      var jenaGraph = {
        '@graph': [].concat(measurementMoments, measurements, studyNode, drugsAndUnits),
        '@context': externalContext
      };
      return jenaGraph;
    }

    function createSimpleStudy(workbook) {
      var studyDataSheet = workbook.Sheets['Study data'];
      var epochs = buildEpochs();
      var studyNode = readStudy(studyDataSheet, epochs, 1);
      studyNode.has_primary_epoch = epochs[0]['@id'];
      var variableColumns = findVariableStartColumns(studyDataSheet, 1);
      var variables = readVariables(studyDataSheet, variableColumns, workbook, 1);
      var measurementMoments = buildSingleSheetMeasurementMoments(variables, epochs);
      variables = measurementMomentLabelToUri(variables, measurementMoments);
      studyNode.has_outcome = variables;
      var measurements = readMeasurements(studyDataSheet, variables, studyNode.has_arm.concat(studyNode.has_included_population), variableColumns, 1);
      var jenaGraph = {
        '@graph': [].concat(measurementMoments, measurements, studyNode),
        '@context': externalContext
      };
      return jenaGraph;
    }

    function uploadExcel(uploadedElement, scope, validityChecker, getIdentifier, names) {
      var file = uploadedElement.files[0];
      var workbook;
      scope.excelUpload = undefined;
      scope.errors = [];
      if (!file) {
        return;
      }
      var reader = new FileReader();
      reader.onload = function(file) {
        var data = file.target.result;
        try {
          workbook = XLSX.read(data, {
            sheetStubs: true,
            type: 'binary'
          });
          scope.errors = validityChecker(workbook);
        } catch (error) {
          scope.errors.push('Cannot parse excel file: ' + error);
        }
        if (!scope.errors.length) {
          scope.excelUpload = workbook;
          scope.isValidUpload = true;
          var identifier = getIdentifier(workbook);
          scope.isUniqueIdentifier = names.indexOf(identifier) === -1;
        }
        $timeout(function() {}, 0); // ensures errors are rendered in the html
      };
      reader.readAsBinaryString(file);
    }

    function createDataset(workbook) {
      var description = workbook.Sheets['Dataset information'].C2 ? workbook.Sheets['Dataset information'].C2.v : undefined;
      return {
        title: workbook.Sheets['Dataset information'].A2.v,
        description: description
      };
    }

    function createDatasetStudies(workbook) {
      var studyIdCells = getStudyIdCells(workbook);
      var startRowsPerStudy = getStartRowsPerStudy(workbook, studyIdCells);
      return _.map(startRowsPerStudy, function(startRows) {
        return createStructuredStudy(workbook, startRows);
      });
    }

    function createDatasetConcepts(workbook) {
      var conceptTypes = {
        Variable: 'ontology:Variable',
        Unit: 'ontology:Unit',
        Drug: 'ontology:Drug'
      };
      var datasetConceptsSheet = workbook.Sheets['Dataset concepts'];
      var concepts = [];
      var index = 1;
      while (datasetConceptsSheet[IOU.a1Coordinate(0, index)]) {
        concepts.push({
          label: datasetConceptsSheet[IOU.a1Coordinate(1, index)].v,
          '@id': datasetConceptsSheet[IOU.a1Coordinate(0, index)].v,
          '@type': conceptTypes[datasetConceptsSheet[IOU.a1Coordinate(2, index)].v]
        });
        ++index;
      }
      return concepts;
    }

    //private
    function checkSheets(workbook) {
      var sheetNames = _.keys(workbook.Sheets);
      if (_.difference(sheetNames, ['Study data']).length !== 0 && _.difference(STUDY_SHEET_NAMES, sheetNames).length !== 0) {
        return 'Excel file should either only have a Study data worksheet, or all required worksheets';
      }
    }

    function checkDatasetSheets(workbook) {
      var allSheetNames = STUDY_SHEET_NAMES.concat(
        'Dataset information',
        'Dataset concepts'
      );
      var sheetNames = _.keys(workbook.Sheets);
      var missingSheets = _.difference(allSheetNames, sheetNames);
      if (missingSheets.length !== 0) {
        return 'Missing worksheets: ' + missingSheets.join(', ');
      }
    }

    function getStudyIdCells(workbook) {
      var studyDataSheet = workbook.Sheets['Study data'];
      var lastRow = excelUtils.decode_range(studyDataSheet['!ref']).e.r;
      return _(_.range(1, lastRow))
        .map(function(row) {
          return {
            value: studyDataSheet[IOU.a1Coordinate(0, row)],
            coords: {
              c: 0,
              r: row,
              a1: IOU.a1Coordinate(0, row)
            }
          };
        })
        .filter('value')
        .filter(function(val, index) {
          return index % 2 === 1; // remove 'id' cells
        })
        .value();
    }

    function getStartRowsPerStudy(workbook, studyIdCells) {
      return _.map(studyIdCells, function(studyIdCell) {
        var startRows = _.reduce(STUDY_SHEET_NAMES.slice(1), function(accum, sheet) { // don't look in the Study data sheet
          accum[sheet] = findStartRow(workbook.Sheets[sheet], studyIdCell);
          return accum;
        }, {
          'Study data': studyIdCell.coords.r - 2
        });
        return startRows;
      });
    }

    function findStartRow(sheet, studyIdCell) {
      var lastRow = excelUtils.decode_range(sheet['!ref']).e.r;
      return _.find(_.range(0, lastRow), function(row) {
        var targetFormula;
        if (sheet[IOU.a1Coordinate(0, row)] && sheet[IOU.a1Coordinate(0, row)].f) {
          targetFormula = sheet[IOU.a1Coordinate(0, row)].f;
          if (targetFormula[0] === '=') {
            targetFormula = targetFormula.split('=')[1];
          }
        }
        return targetFormula === '\'Study data\'!' + studyIdCell.coords.a1;
      });
    }

    function buildDrugsAndUnits(conceptSheet, workbook, startRow) {
      var lastRow = startRow;
      while (conceptSheet[IOU.a1Coordinate(0, lastRow)]) {
        lastRow++;
      } // one more because _.range goes until, not including its second parameter 
      return _(_.range(startRow + 1, lastRow))
        .filter(function(row) {
          var type = IOU.getValue(conceptSheet, 2, row);
          return type === 'drug' || type === 'unit';
        })
        .map(_.partial(readDrugOrUnit, conceptSheet, workbook))
        .value();
    }

    function readDrugOrUnit(conceptSheet, workbook, row) {
      var type = IOU.getValue(conceptSheet, 2, row);
      var mapping = IOU.getValueIfPresent(conceptSheet, 3, row);
      var concept = {};
      if (mapping) {
        concept.sameAs = mapping;
      }
      if (type === 'drug') {
        _.extend(concept, {
          '@id': IOU.getValue(conceptSheet, 0, row),
          '@type': 'ontology:Drug',
          label: IOU.getValue(conceptSheet, 1, row)
        });
      } else if (type === 'unit') {
        _.extend(concept, {
          '@id': IOU.getValue(conceptSheet, 0, row),
          '@type': 'ontology:Unit',
          label: IOU.getValue(conceptSheet, 1, row),
          conversionMultiplier: IOU.getValueIfPresent(conceptSheet, 4, row)
        });
      }
      return concept;
    }

    function buildActivities(activitySheet, workbook, startRow) {
      var lastRow = startRow;
      while (activitySheet[IOU.a1Coordinate(0, lastRow)]) {
        ++lastRow;
      } // one more because _.range goes until, not including its second parameter 
      return _.map(_.range(startRow + 1, lastRow), _.partial(readActivity, activitySheet, workbook)); // +1 because zero-indexed and range has open upper end
    }

    function readActivity(activitySheet, workbook, row) {
      var activityTypesByUri = _.keyBy(ACTIVITY_TYPE_OPTIONS, 'label');
      var activity = {
        '@id': IOU.getValue(activitySheet, 0, row),
        '@type': activityTypesByUri[IOU.getValue(activitySheet, 2, row)].uri,
        label: IOU.getValue(activitySheet, 1, row),
        has_activity_application: []
      };
      IOU.assignIfPresent(activity, 'comment', activitySheet, 3, row);

      var drugIndex = 0;
      while (activitySheet[IOU.a1Coordinate(4 + drugIndex * 6, row)]) {
        activity.has_drug_treatment = activity.has_drug_treatment ? activity.has_drug_treatment : [];
        var drugTreatment = readDrugTreatment(activitySheet, drugIndex, row, workbook);
        ++drugIndex;
        activity.has_drug_treatment = [].concat(activity.has_drug_treatment, drugTreatment);
      }
      return activity;
    }

    function readDrugTreatment(activitySheet, drugIndex, row, workbook) {
      var doseTypes = {
        'fixed': 'ontology:FixedDoseDrugTreatment',
        'titrated': 'ontology:TitratedDoseDrugTreatment'
      }; 
      var treatment = {
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        treatment_has_drug: IOU.getReferenceValueColumnOffset(activitySheet, 4 + drugIndex * 6, row, -1, workbook),
        '@type': doseTypes[IOU.getValue(activitySheet, 5 + drugIndex * 6, row)]
      };
      var treatmentType = treatment['@type'];
      if (treatmentType === doseTypes.fixed) {
        treatment.treatment_dose = buildTreatmentDose(activitySheet, 6 + drugIndex * 6, 0, row, workbook);
      } else {
        treatment.treatment_min_dose = buildTreatmentDose(activitySheet, 6 + drugIndex * 6, 0, row, workbook);
        treatment.treatment_max_dose = buildTreatmentDose(activitySheet, 6 + drugIndex * 6, 1, row, workbook);
      }
      return treatment;
    }

    function buildTreatmentDose(sheet, baseColumn, valueOffset, row, workbook) {
      return [{
        '@id': INSTANCE_PREFIX + UUIDService.generate(),
        value: IOU.getValue(sheet, baseColumn + valueOffset, row),
        unit: IOU.getReferenceValueColumnOffset(sheet, baseColumn + 2, row, -1, workbook),
        dosingPeriodicity: IOU.getValue(sheet, baseColumn + 3, row)
      }];
    }

    function addStudyDesign(studyNode, workbook, startRow) {
      var studyDesignSheet = workbook.Sheets['Study design'];
      return _.map(studyNode.has_activity, function(activity) {
        var rowOffset = workbook.Sheets['Dataset information'] ? 1 : 0; // Dataset or single study
        var rowIndex = startRow + rowOffset;
        var applicationsForActivity = [];
        while (studyDesignSheet[IOU.a1Coordinate(0, rowIndex)]) {
          var columnIndex = 0;
          while (studyDesignSheet[IOU.a1Coordinate(columnIndex, rowIndex)]) {
            var offset = columnIndex === 0 ? 0 : -1;
            if (studyDesignSheet[IOU.a1Coordinate(columnIndex, rowIndex)].f &&
              IOU.getReferenceValueColumnOffset(studyDesignSheet, columnIndex, rowIndex, offset, workbook) === activity['@id']) {
              applicationsForActivity.push({
                c: columnIndex,
                r: rowIndex
              });
            }
            columnIndex++;
          }
          rowIndex++;
        }

        return _.merge({}, activity, {
          has_activity_application: _.map(applicationsForActivity, function(applicationCell) {
            var armLabel = IOU.getReferenceValueColumnOffset(studyDesignSheet, 0, applicationCell.r, 0, workbook);
            var arm = _.find(studyNode.has_arm, ['label', armLabel]);
            return {
              '@id': INSTANCE_PREFIX + UUIDService.generate(),
              applied_in_epoch: IOU.getReferenceValueColumnOffset(studyDesignSheet, applicationCell.c, startRow + rowOffset, -1, workbook),
              applied_to_arm: arm['@id']
            };
          })
        });
      });
    }

    function findVariableStartColumns(studyDataSheet, startRow) {
      var startColumn = 12; // =>'M'
      var endColumn = XLSX.utils.decode_range(studyDataSheet['!ref']).e.c;
      return _.filter(_.range(startColumn, endColumn), function(column) {
        return studyDataSheet[IOU.a1Coordinate(column, startRow)];
      });
    }

    function buildStructuredMeasurementMoments(measurementMomentSheet, workbook, startRow) {
      var lastRow = startRow;
      while (measurementMomentSheet[IOU.a1Coordinate(0, lastRow)]) {
        ++lastRow;
      } // one more because _.range goes until, not including its second parameter 
      return _.map(_.range(startRow + 1, lastRow), _.partial(readMeasurementMoment, measurementMomentSheet, workbook));
    }

    function readMeasurementMoment(measurementMomentSheet, workbook, row) {
      return {
        '@id': IOU.getValue(measurementMomentSheet, 0, row),
        '@type': 'ontology:MeasurementMoment',
        label: IOU.getValue(measurementMomentSheet, 1, row),
        relative_to_epoch: IOU.getReferenceValueColumnOffset(measurementMomentSheet, 2, row, -1, workbook),
        relative_to_anchor: EPOCH_ANCHOR[IOU.getValue(measurementMomentSheet, 3, row)].uri,
        time_offset: IOU.getValue(measurementMomentSheet, 4, row)
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

    function buildEpochs(epochSheet, startRow) {
      if (!epochSheet) {
        return [{
          '@id': INSTANCE_PREFIX + UUIDService.generate(),
          '@type': 'ontology:Epoch',
          label: 'Automatically generated primary epoch',
          duration: 'PT0S'
        }];
      } else {
        var lastRow = startRow;
        while (epochSheet[IOU.a1Coordinate(0, lastRow)]) {
          ++lastRow;
        } // one more because _.range goes until, not including its second parameter 
        return _.map(_.range(startRow + 1, lastRow), _.partial(readEpoch, epochSheet)); //+1 as first row is study header
      }
    }

    function readEpoch(epochSheet, row) {
      var epoch = {
        '@id': IOU.getValue(epochSheet, 0, row),
        '@type': 'ontology:Epoch',
        label: IOU.getValue(epochSheet, 1, row),
        duration: IOU.getValue(epochSheet, 3, row),
        isPrimary: IOU.getValue(epochSheet, 4, row)
      };
      IOU.assignIfPresent(epoch, 'comment', epochSheet, 2, row);
      return epoch;
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

    function readVariables(studyDataSheet, variableColumns, workbook, startRow) {
      var endColumn = XLSX.utils.decode_range(studyDataSheet['!ref']).e.c + 1; // +1 for _.range being open at upper end
      var lastFilledColumn = _.max(_.filter(_.range(variableColumns[variableColumns.length - 1], endColumn), function(column) {
        return studyDataSheet[IOU.a1Coordinate(column, startRow + 1)];
      }));
      var variableColumnsPlusEnd = variableColumns.concat(lastFilledColumn +  1); // add end column for last variable; +1 because we use [,) intervals for var boundaries
      var variableColumnBoundaries = _.map(variableColumnsPlusEnd.slice(0, variableColumnsPlusEnd.length - 1), function(n, index) {
        return [n, variableColumnsPlusEnd[index + 1]];
      });
      var variables = _.map(variableColumnBoundaries, function(columns) {
        var variable = readVariable(
          studyDataSheet,
          columns,
          getVariableFactory(studyDataSheet, columns, workbook, startRow),
          getMeasurementMomentReader(studyDataSheet, workbook, startRow),
          startRow);
        if (workbook && workbook.Sheets.Concepts) {
          try {
            variable.of_variable[0].sameAs = IOU.getReferenceValueColumnOffset(studyDataSheet, columns[0], startRow, 2, workbook);
          } catch (e) {
            // no mapping
          }
        }
        return variable;
      });
      return variables;
    }

    function getVariableFactory(studyDataSheet, columns, workbook, startRow) {
      if (workbook && workbook.Sheets.Concepts) {
        return function() {
          return {
            '@id': IOU.getReferenceValueColumnOffset(studyDataSheet, columns[0], startRow, -1, workbook),
            '@type': findOntology(VARIABLE_TYPES, IOU.getValueIfPresent(studyDataSheet, columns[0], startRow + 2)),
            label: IOU.getReferenceValue(studyDataSheet, columns[0], startRow, workbook)
          };
        };
      } else {
        return function() {
          return {
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            '@type': findOntology(VARIABLE_TYPES, IOU.getValueIfPresent(studyDataSheet, columns[0], startRow + 2)),
            label: IOU.getValueIfPresent(studyDataSheet, columns[0], startRow)
          };
        };
      }
    }

    function getMeasurementMomentReader(studyDataSheet, workbook, startRow) {
      if (workbook && workbook.Sheets.Concepts) {
        return function(column) {
          return IOU.getReferenceValueColumnOffset(studyDataSheet, column, startRow + 2, -1, workbook);
        };
      } else {
        return function(column) {
          return IOU.getValueIfPresent(studyDataSheet, column, startRow + 2);
        };
      }
    }

    function readVariable(studyDataSheet, columns, variableFactory, measurementMomentReader, startRow) {
      var newVariable = variableFactory();
      var measurementType = MEASUREMENT_TYPES[IOU.getValueIfPresent(studyDataSheet, columns[0] + 1, startRow + 2)].uri;
      var ofVariable = {
        '@type': 'ontology:Variable',
        measurementType: measurementType,
        label: newVariable.label
      };
      if (measurementType === MEASUREMENT_TYPES.categorical.uri) {
        var categoryNames = readDataColumnNames(studyDataSheet, columns, _.identity, startRow);
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
        }, startRow);
        if (newVariable.measurementType === 'ontology:survival' && newVariable.has_result_property.indexOf(ONTOLOGY_PREFIX + 'exposure') > -1) {
          newVariable.timeScale = IOU.getValue(studyDataSheet, columns[0] + 2, startRow + 2);
        }
      }
      newVariable.of_variable = [ofVariable];
      newVariable.is_measured_at = readMeasurementMoments(studyDataSheet, columns, measurementMomentReader, startRow);
      return newVariable;
    }

    function readDataColumnNames(studyDataSheet, columns, prefixer, startRow) {
      return _(_.range(columns[0] + 2, columns[1]))
        .map(function(column) {
          return IOU.getValueIfPresent(studyDataSheet, column, startRow + 1);
        })
        .without('measurement moment')
        .without('time scale')
        .uniq()
        .map(prefixer)
        .value();
    }

    function readMeasurementMoments(studyDataSheet, columns, measurementMomentReader, startRow) {
      return _(_.range(columns[0] + 1, columns[1]))
        .filter(function(column) {
          return IOU.getValueIfPresent(studyDataSheet, column, startRow + 1) === 'measurement moment';
        })
        .map(function(column) {
          return measurementMomentReader(column);
        })
        .uniq()
        .value();
    }

    function readMeasurements(studyDataSheet, variables, arms, columns, startRow) {
      var measurements = [];
      _.forEach(variables, function(variable, variableIndex) {
        var variableColumn = columns[variableIndex];
        _.forEach(arms, function(arm, armIndex) {
          var currentY = startRow + 2 + armIndex;
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
        var currentValue = IOU.getValueIfPresent(studyDataSheet, currentX, currentY);
        if (currentValue !== undefined && currentValue !== null) {
          measurement = readMeasurementValue(measurement, resultColumn, currentValue);
        }
      });
      return measurement;
    }

    function readStudy(studyDataSheet, epochs, startRow) {
      var firstDataRow = startRow + 2;
      var study = {
        '@id': 'http://trials.drugis.org/studies/' + UUIDService.generate(),
        '@type': 'ontology:Study',
        label: studyDataSheet[IOU.a1Coordinate(0, firstDataRow)].v,
        comment: studyDataSheet[IOU.a1Coordinate(2, firstDataRow)].v,
        has_allocation: studyDataSheet[IOU.a1Coordinate(3, firstDataRow)] ? findOntology(GROUP_ALLOCATION_OPTIONS, studyDataSheet[IOU.a1Coordinate(3, firstDataRow)].v) : undefined,
        has_blinding: studyDataSheet[IOU.a1Coordinate(4, firstDataRow)] ? findOntology(BLINDING_OPTIONS, studyDataSheet[IOU.a1Coordinate(4, firstDataRow)].v) : undefined,
        status: studyDataSheet[IOU.a1Coordinate(5, firstDataRow)] ? findOntology(STATUS_OPTIONS, studyDataSheet[IOU.a1Coordinate(5, firstDataRow)].v) : undefined,
        has_number_of_centers: studyDataSheet[IOU.a1Coordinate(6, firstDataRow)] ? studyDataSheet[IOU.a1Coordinate(6, firstDataRow)].v : undefined,
        has_objective: getCommented(studyDataSheet, IOU.a1Coordinate(7, firstDataRow)),
        has_indication: getLabeled(studyDataSheet, IOU.a1Coordinate(8, firstDataRow)),
        has_eligibility_criteria: getCommented(studyDataSheet, IOU.a1Coordinate(9, firstDataRow)),
        has_activity: [],
        has_arm: readArms(studyDataSheet, firstDataRow),
        has_epochs: RdfListService.unFlattenList(epochs),
        has_group: [],
        has_included_population: createIncludedPopulation(),
        has_outcome: [],
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

    function commitStudy(study, datasetUuid) {
      var newVersionDefer = $q.defer();
      GraphResource.putJson({
        userUid: $stateParams.userUid,
        datasetUuid: datasetUuid ? datasetUuid : $stateParams.datasetUuid,
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

    function readArms(studyDataSheet, headerRow) {
      var index = headerRow + 1; // +1 because we're using A1 below
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

    // interface
    return {
      checkSingleStudyWorkbook: checkSingleStudyWorkbook,
      createStudy: createStudy,
      commitStudy: commitStudy,
      
      uploadExcel: uploadExcel,
      
      checkDatasetWorkbook: checkDatasetWorkbook,
      createDataset: createDataset,
      createDatasetStudies: createDatasetStudies,
      createDatasetConcepts: createDatasetConcepts
    };

  };
  return dependencies.concat(ExcelImportService);
});
