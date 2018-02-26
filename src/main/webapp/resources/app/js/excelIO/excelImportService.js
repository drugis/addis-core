'use strict';
define(['lodash', 'util/context', 'util/constants', 'xlsx-shim'], function(_, externalContext, constants, XLSX) {
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
          'Study data': 4,
          'Activities': 1,
          'Epochs': 1,
          'Study design': 0,
          'Measurement moments': 1,
          'Concepts': 1
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
      var measurementMoments = buildStructuredMeasurementMoments(measurementMomentSheet, workbook);

      var studyNode = readStudy(studyDataSheet, epochs);
      studyNode.has_primary_epoch = primaryEpoch ? primaryEpoch['@id'] : undefined;
      studyNode.has_activity = buildActivities(workbook.Sheets.Activities, workbook);
      var variableColumns = findVariableStartColumns(studyDataSheet);
      var variables = readVariables(studyDataSheet, variableColumns, workbook);
      var measurements = readMeasurements(studyDataSheet, variables,
        studyNode.has_arm.concat(studyNode.has_included_population), variableColumns);
      studyNode.has_outcome = variables;
      studyNode.has_activity = addStudyDesign(studyNode, workbook);
      var drugsAndUnits = buildDrugsAndUnits(workbook.Sheets.Concepts, workbook);
      var jenaGraph = {
        '@graph': [].concat(measurementMoments, measurements, studyNode, drugsAndUnits),
        '@context': externalContext
      };
      return jenaGraph;
    }

    function createSimpleStudy(workbook) {
      var studyDataSheet = workbook.Sheets['Study data'];
      var epochs = buildEpochs(workbook.Sheets.Epochs);
      var studyNode = readStudy(studyDataSheet, epochs, 4);
      studyNode.has_primary_epoch = epochs[0]['@id'];
      var variableColumns = findVariableStartColumns(studyDataSheet, 4);
      var variables = readVariables(studyDataSheet, variableColumns, 4);
      var measurementMoments = buildSingleSheetMeasurementMoments(variables, epochs);
      variables = measurementMomentLabelToUri(variables, measurementMoments);
      var measurements = readMeasurements(studyDataSheet, variables, studyNode.has_arm.concat(studyNode.has_included_population), variableColumns, 4);
      studyNode.has_outcome = variables;
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
      // var studyDataSheet = workbook['Study data'];
      var studyIdCells = getStudyIdCells(workbook);
      var startRows = getStartRows(workbook, studyIdCells);
      return _.map(startRows, function(startRow) {
        return createStructuredStudy(workbook, startRow);
      });
    }

    //private
    function checkSheets(workbook) {
      var sheetNames = _.keys(workbook.Sheets);
      if (_.difference(sheetNames, ['Study data']).length !== 0 && _.difference(sheetNames, STUDY_SHEET_NAMES).length !== 0) {
        return 'Excel file should either only have a Study data worksheet, or all required worksheets';
      }
    }

    function checkDatasetSheets(workbook) {
      var allSheetNames = STUDY_SHEET_NAMES.concat(
        'Dataset information',
        'Dataset concepts'
      );
      var sheetNames = _.keys(workbook.Sheets);
      var missingSheets = _.difference(sheetNames, allSheetNames);
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
            value: studyDataSheet[a1Coordinate(0, row)],
            coords: {
              c: 0,
              r: row,
              a1: a1Coordinate(0, row)
            }
          };
        })
        .filter('value')
        .filter(function(val, index) {
          return index % 2 === 1; // remove 'id' cells
        })
        .value();
    }

    function getStartRows(workbook, studyIdCells) {
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
        if (sheet[a1Coordinate(0, row)] && sheet[a1Coordinate(0, row)].f) {
          targetFormula = sheet[a1Coordinate(0, row)].f;
          if (targetFormula[0] === '=') {
            targetFormula = targetFormula.split('=')[1];
          }
        }
        return targetFormula === '\'Study data\'!' + studyIdCell.coords.a1;
      });
    }

    function buildDrugsAndUnits(conceptSheet, workbook) {
      var lastRow = excelUtils.decode_range(conceptSheet['!ref']).e.r;
      return _(_.range(1, lastRow + 1))
        .filter(function(row) {
          var type = getValue(conceptSheet, 2, row);
          return type === 'drug' || type === 'unit';
        })
        .map(_.partial(readDrugOrUnit, conceptSheet, workbook))
        .value();
    }

    function readDrugOrUnit(conceptSheet, workbook, row) {
      var type = getValue(conceptSheet, 2, row);
      var mapping = getValueIfPresent(conceptSheet, 3, row);
      var concept = {};
      if (mapping) {
        concept.sameAs = mapping;
      }
      if (type === 'drug') {
        _.extend(concept, {
          '@id': getValue(conceptSheet, 0, row),
          '@type': 'ontology:Drug',
          label: getValue(conceptSheet, 1, row)
        });
      } else if (type === 'unit') {
        _.extend(concept, {
          '@id': getValue(conceptSheet, 0, row),
          '@type': 'ontology:Unit',
          label: getValue(conceptSheet, 1, row),
          conversionMultiplier: getValueIfPresent(conceptSheet, 4, row)
        });
      }
      return concept;
    }

    function buildActivities(activitySheet, workbook) {
      var lastRow = excelUtils.decode_range(activitySheet['!ref']).e.r;
      return _.map(_.range(1, lastRow + 1), _.partial(readActivity, activitySheet, workbook)); // +2 because zero-indexed and range has open upper end

    }

    function readActivity(activitySheet, workbook, row) {
      var activityTypesByUri = _.keyBy(constants.ACTIVITY_TYPE_OPTIONS, 'label');
      var activity = {
        '@id': getValue(activitySheet, 0, row),
        '@type': activityTypesByUri[getValue(activitySheet, 2, row)].uri,
        label: getValue(activitySheet, 1, row),
        has_activity_application: []
      };
      assignIfPresent(activity, 'comment', activitySheet, 3, row);

      var drugIndex = 0;
      while (activitySheet[a1Coordinate(4 + drugIndex * 6, row)]) {
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
        treatment_has_drug: getReferenceValueColumnOffset(activitySheet, 4 + drugIndex * 6, row, -1, workbook),
        '@type': doseTypes[getValue(activitySheet, 5 + drugIndex * 6, row)]
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
        value: getValue(sheet, baseColumn + valueOffset, row),
        unit: getReferenceValueColumnOffset(sheet, baseColumn + 2, row, -1, workbook),
        dosingPeriodicity: getValue(sheet, baseColumn + 3, row)
      }];
    }

    function addStudyDesign(studyNode, workbook) {
      var studyDesignSheet = workbook.Sheets['Study design'];
      return _.map(studyNode.has_activity, function(activity) {
        var applicationsForActivity = _.reduce(studyDesignSheet, function(accum, cell, cellKey) {
          var decodedCell = excelUtils.decode_cell(cellKey);
          var offset = decodedCell.c === 0 ? 0 : -1; // First row contains arms, they do not need an offset 
          if (cell.f && getReferenceValueColumnOffset(studyDesignSheet, decodedCell.c, decodedCell.r, offset, workbook) === activity['@id']) {
            accum.push(decodedCell);
          }
          return accum;
        }, []);

        return _.merge({}, activity, {
          has_activity_application: _.map(applicationsForActivity, function(applicationCell) {
            return {
              '@id': INSTANCE_PREFIX + UUIDService.generate(),
              applied_in_epoch: getReferenceValueColumnOffset(studyDesignSheet, applicationCell.c, 0, -1, workbook),
              applied_to_arm: _.find(studyNode.has_arm, ['label', getReferenceValueColumnOffset(studyDesignSheet, 0, applicationCell.r, 0, workbook)])['@id']
            };
          })
        });
      });
    }

    function assignIfPresent(object, field, sheet, column, row) {
      var value = getValueIfPresent(sheet, column, row);
      if (value) {
        object[field] = value;
      }
    }

    function findVariableStartColumns(studyDataSheet) {
      var startColumn = 12; // =>'M'
      var endColumn = XLSX.utils.decode_range(studyDataSheet['!ref']).e.c;
      return _.filter(_.range(startColumn, endColumn), function(column) {
        return studyDataSheet[a1Coordinate(column, 1)];
      });
    }

    function buildStructuredMeasurementMoments(measurementMomentSheet, workbook) {
      var lastRow = excelUtils.decode_range(measurementMomentSheet['!ref']).e.r;
      return _.map(_.range(1, lastRow + 1), _.partial(readMeasurementMoment, measurementMomentSheet, workbook)); // +2 because zero-indexed and range has open upper end
    }

    function readMeasurementMoment(measurementMomentSheet, workbook, row) {
      return {
        '@id': getValue(measurementMomentSheet, 0, row),
        '@type': 'ontology:MeasurementMoment',
        label: getValue(measurementMomentSheet, 1, row),
        relative_to_epoch: getReferenceValue(measurementMomentSheet, 2, row, workbook),
        relative_to_anchor: EPOCH_ANCHOR[getValue(measurementMomentSheet, 3, row)].uri,
        time_offset: getValue(measurementMomentSheet, 4, row)
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
        while (epochSheet[a1Coordinate(0, lastRow)]) {
          lastRow++;
        } // one more because _.range goes until, not including its second parameter 
        return _.map(_.range(startRow+1, lastRow), _.partial(readEpoch, epochSheet)); //+1 as first row is study header
      }
    }

    function readEpoch(epochSheet, row) {
      var epoch = {
        '@id': getValue(epochSheet, 0, row),
        '@type': 'ontology:Epoch',
        label: getValue(epochSheet, 1, row),
        duration: getValue(epochSheet, 3, row),
        isPrimary: getValue(epochSheet, 4, row)
      };
      assignIfPresent(epoch,'comment', epochSheet, 2, row);
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

    function readVariables(studyDataSheet, variableColumns, workbook) {
      var endColumn = XLSX.utils.decode_range(studyDataSheet['!ref']).e.c + 1;
      var variableColumnsPlusEnd = variableColumns.concat(endColumn); // add end column for last variable
      var variableColumnBoundaries = _.map(variableColumnsPlusEnd.slice(0, variableColumnsPlusEnd.length - 1), function(n, index) {
        return [n, variableColumnsPlusEnd[index + 1]];
      });
      var variables = _.map(variableColumnBoundaries, function(columns) {
        var variable = readVariable(studyDataSheet, columns,
          getVariableFactory(studyDataSheet, columns, workbook),
          getMeasurementMomentReader(studyDataSheet, workbook));
        if (workbook && workbook.Sheets.Concepts) {
          try {
            variable.of_variable[0].sameAs = getReferenceValueColumnOffset(studyDataSheet, columns[0], 1, 2, workbook);
          } catch (e) {
            // no mapping
          }
        }
        return variable;
      });
      return variables;
    }

    function getVariableFactory(studyDataSheet, columns, workbook) {
      if (workbook && workbook.Sheets.Concepts) {
        return function() {
          return {
            '@id': getReferenceValueColumnOffset(studyDataSheet, columns[0], 1, -1, workbook),
            '@type': findOntology(VARIABLE_TYPES, getValueIfPresent(studyDataSheet, columns[0], 3)),
            label: getReferenceValue(studyDataSheet, columns[0], 1, workbook)
          };
        };
      } else {
        return function() {
          return {
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            '@type': findOntology(VARIABLE_TYPES, getValueIfPresent(studyDataSheet, columns[0], 3)),
            label: getValueIfPresent(studyDataSheet, columns[0], 1)
          };
        };
      }
    }

    function getMeasurementMomentReader(studyDataSheet, workbook) {
      if (workbook && workbook.Sheets.Concepts) {
        return function(column) {
          return getReferenceValueColumnOffset(studyDataSheet, column, 3, -1, workbook);
        };
      } else {
        return function(column) {
          return getValueIfPresent(studyDataSheet, column, 3);
        };
      }
    }

    function readVariable(studyDataSheet, columns, variableFactory, measurementMomentReader) {
      var newVariable = variableFactory();
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
        if (newVariable.measurementType === 'ontology:survival' && newVariable.has_result_property.indexOf(ONTOLOGY_PREFIX + 'exposure') > -1) {
          newVariable.timeScale = getValue(studyDataSheet, columns[0] + 2, 3);
        }
      }
      newVariable.of_variable = [ofVariable];
      newVariable.is_measured_at = readMeasurementMoments(studyDataSheet, columns, measurementMomentReader);
      return newVariable;
    }

    function readDataColumnNames(studyDataSheet, columns, prefixer) {
      return _(_.range(columns[0] + 2, columns[1]))
        .map(function(column) {
          return getValueIfPresent(studyDataSheet, column, 2);
        })
        .without('measurement moment')
        .without('time scale')
        .uniq()
        .map(prefixer)
        .value();
    }

    function readMeasurementMoments(studyDataSheet, columns, measurementMomentReader) {
      return _(_.range(columns[0] + 1, columns[1]))
        .filter(function(column) {
          return getValueIfPresent(studyDataSheet, column, 2) === 'measurement moment';
        })
        .map(function(column) {
          return measurementMomentReader(column);
        })
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
      return getReferenceValueColumnOffset(sourceSheet, column, row, 0, workbook);
    }

    function getReferenceValueColumnOffset(sourceSheet, column, row, columnOffset, workbook) {
      var source = sourceSheet[a1Coordinate(column, row)];
      if (source) {
        var splitFormula = source.f.split('!');
        var targetSheet = splitFormula[0];
        if (targetSheet[0] === '=') {
          targetSheet = targetSheet.split('=')[1];
        }
        var targetCoordinates = excelUtils.decode_cell(splitFormula[1]);
        targetCoordinates.c += columnOffset;
        targetCoordinates = excelUtils.encode_cell(targetCoordinates);
        targetSheet = targetSheet.replace(/\'/g, '');
        if (workbook.Sheets[targetSheet] && workbook.Sheets[targetSheet][targetCoordinates]) {
          return workbook.Sheets[targetSheet][targetCoordinates].v;
        } else {
          throw 'Broken reference: ' + source.f;
        }
      }
    }

    function getValueIfPresent(dataSheet, column, row) {
      var cell = dataSheet[a1Coordinate(column, row)];
      return cell ? cell.v : undefined;
    }

    function getValue(dataSheet, column, row) {
      var cell = dataSheet[a1Coordinate(column, row)];
      return cell.v;
    }

    // interface
    return {
      checkSingleStudyWorkbook: checkSingleStudyWorkbook,
      checkDatasetWorkbook: checkDatasetWorkbook,
      createStudy: createStudy,
      commitStudy: commitStudy,
      uploadExcel: uploadExcel,
      createDataset: createDataset,
      createDatasetStudies: createDatasetStudies
    };

  };
  return dependencies.concat(ExcelImportService);
});