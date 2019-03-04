'use strict';
define(['lodash', 'xlsx'], function(_, XLSX) {
  var dependencies = [
    '$q',
    '$location',
    'GROUP_ALLOCATION_OPTIONS',
    'BLINDING_OPTIONS',
    'STATUS_OPTIONS',
    'ResultsService',
    'ExcelIOUtilService'
  ];
  var ExcelExportService = function(
    $q,
    $location,
    GROUP_ALLOCATION_OPTIONS,
    BLINDING_OPTIONS,
    STATUS_OPTIONS,
    ResultsService,
    IOU
  ) {
    var excelUtils = XLSX.utils;

    function getVariableResults(otherPromises, variableResults) {
      var populationCharacteristics = addConceptType(variableResults[0], 'baseline characteristic');
      var outcomes = addConceptType(variableResults[1], 'outcome');
      var adverseEvents = addConceptType(variableResults[2], 'adverse event');
      var resultsPromises = _.map(populationCharacteristics.concat(outcomes, adverseEvents), function(variable) {
        return ResultsService.queryResults(variable.uri).then(function(results) {
          return {
            uri: variable.uri,
            results: results
          };
        });
      });
      return $q.all(otherPromises.concat([populationCharacteristics, outcomes, adverseEvents], resultsPromises));
    }

    // NB: start of range is always assumed to be constant A1 
    // targetSheet is the old sheet, sourceSheet is the newly made sheet
    function mergePreservingRange(targetSheet, sourceSheet) {
      var sourceRange = excelUtils.decode_range(sourceSheet['!ref']);
      var targetRange;
      if (targetSheet && targetSheet['!ref']) {
        targetRange = excelUtils.decode_range(targetSheet['!ref']);
      } else {
        targetRange = {
          e: {
            r: 0,
            c: 0
          }
        };
      }
      var maxRow = _.max([sourceRange.e.r, targetRange.e.r]);
      var maxCol = _.max([sourceRange.e.c, targetRange.e.c]);
      var resultRange = excelUtils.encode_range({
        s: {
          r: 0,
          c: 0
        },
        e: {
          r: maxRow,
          c: maxCol
        }
      });
      var ref = {
        '!ref': resultRange
      };
      var targetMerges = targetSheet['!merges'] ? targetSheet['!merges'] : [];
      var sourceMerges = sourceSheet['!merges'] ? sourceSheet['!merges'] : [];
      var merges = {
        '!merges': targetMerges.concat(sourceMerges)
      };
      if (!merges['!merges'].length) {
        merges = {};
      }
      return _.merge({}, targetSheet, sourceSheet, ref, merges);
    }

    // ----------- sheet building functions ------------
    function buildStudyDataSheet(startRows, study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
      populationInformation, variables, conceptsSheet, measurementMomentSheet) {
      var startRow = startRows['Study data'];
      var studyDataSheet = initStudyDataSheet();
      var studyHeaders = buildStudyHeaders(startRow);
      var armsPlusOverallPopulation = arms.concat({
        armURI: study.has_included_population[0]['@id']
      });
      var studyData = buildStudyInformation(startRow, study, studyInformation, studyUrl);
      var armData = buildArmData(startRow, arms);
      var populationInformationData = buildPopulationInformationData(startRow, populationInformation);
      var variablesData = buildVariablesData(startRow, variables, armsPlusOverallPopulation, conceptsSheet, measurementMomentSheet);
      var armMerges = buildArmMerges(startRow, armsPlusOverallPopulation);

      _.merge(studyDataSheet, studyHeaders, studyData, populationInformationData, armData, variablesData.data);
      var lastColumn = variables.length ? _(variablesData.data)
        .keys()
        .map(excelUtils.decode_cell)
        .map('c')
        .max() : 12;
      studyDataSheet['!merges'] = studyDataSheet['!merges'].concat([IOU.cellRange(12, 0, lastColumn, 0)], variablesData.merges, armMerges);
      studyDataSheet['!ref'] = 'A1:' + IOU.a1Coordinate(lastColumn, startRow + 3 + arms.length);
      return studyDataSheet;
    }

    function buildActivitiesSheet(startRows, activities, conceptsSheet) {
      var startRow = startRows.Activities;
      var doseTypes = {
        'ontology:FixedDoseDrugTreatment': 'fixed',
        'ontology:TitratedDoseDrugTreatment': 'titrated'
      };

      var sheet = {
        A1: IOU.cellValue('id'),
        B1: IOU.cellValue('title'),
        C1: IOU.cellValue('type'),
        D1: IOU.cellValue('description')
      };

      var maxTreatments = _.max(_.map(activities, function(activity) {
        return activity.treatments ? activity.treatments.length : 0;
      }));
      var colHeaders = _.reduce(_.range(0, maxTreatments), function(accum, i) {
        accum[IOU.a1Coordinate(4 + i * 6, 0)] = IOU.cellValue('drug label');
        accum[IOU.a1Coordinate(5 + i * 6, 0)] = IOU.cellValue('dose type');
        accum[IOU.a1Coordinate(6 + i * 6, 0)] = IOU.cellValue('dose');
        accum[IOU.a1Coordinate(7 + i * 6, 0)] = IOU.cellValue('max dose');
        accum[IOU.a1Coordinate(8 + i * 6, 0)] = IOU.cellValue('unit');
        accum[IOU.a1Coordinate(9 + i * 6, 0)] = IOU.cellValue('periodicity');
        return accum;
      }, {});

      var activityData = _.reduce(activities, function(accum, activity, index) {
        var row = startRow + index + 1;
        accum[IOU.a1Coordinate(0, row)] = IOU.cellValue(activity.activityUri);
        accum[IOU.a1Coordinate(1, row)] = IOU.cellValue(activity.label);
        accum[IOU.a1Coordinate(2, row)] = IOU.cellValue(activity.activityType.label);
        accum[IOU.a1Coordinate(3, row)] = IOU.cellValue(activity.activityDescription);

        if (activity.activityType.uri === 'ontology:TreatmentActivity') {
          _.forEach(activity.treatments, function(treatment, index) {
            var isFixedDose = treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment';
            var drugReference = IOU.getTitleReference(conceptsSheet, treatment.drug.uri);
            var unitReference = IOU.getTitleReference(conceptsSheet, treatment.doseUnit.uri);
            accum[IOU.a1Coordinate(4 + index * 6, row)] = IOU.cellFormula('Concepts!' + drugReference);
            accum[IOU.a1Coordinate(5 + index * 6, row)] = IOU.cellValue(doseTypes[treatment.treatmentDoseType]);
            accum[IOU.a1Coordinate(6 + index * 6, row)] = IOU.cellNumber(isFixedDose ? treatment.fixedValue : treatment.minValue);
            accum[IOU.a1Coordinate(7 + index * 6, row)] = IOU.cellNumber(isFixedDose ? undefined : treatment.maxValue);
            accum[IOU.a1Coordinate(8 + index * 6, row)] = IOU.cellFormula('Concepts!' + unitReference);
            accum[IOU.a1Coordinate(9 + index * 6, row)] = IOU.cellValue(treatment.dosingPeriodicity);
          });
        }
        return accum;
      }, {});

      return _.merge(IOU.cellReference('A1:' + IOU.a1Coordinate(4 + maxTreatments * 6, startRow + activities.length)), sheet, colHeaders, activityData);
    }

    function buildEpochSheet(startRows, epochs) {
      var startRow = startRows.Epochs;
      var epochHeaders = {
        A1: IOU.cellValue('id'),
        B1: IOU.cellValue('name'),
        C1: IOU.cellValue('description'),
        D1: IOU.cellValue('duration'),
        E1: IOU.cellValue('Is primary?')
      };
      var epochData = _.reduce(epochs, function(accum, epoch, index) {
        var row = startRow + index + 2;
        accum['A' + row] = IOU.cellValue(epoch.uri);
        accum['B' + row] = IOU.cellValue(epoch.label);
        accum['C' + row] = IOU.cellValue(epoch.comment);
        accum['D' + row] = IOU.cellValue(epoch.duration);
        accum['E' + row] = IOU.cellValue(epoch.isPrimary);
        return accum;
      }, {});

      return _.merge(IOU.cellReference('A1:E' + (startRow + epochs.length + 1)), epochHeaders, epochData);
    }

    function buildStudyDesignSheet(startRows, epochs, arms, studyDesign, epochSheet, activitiesSheet, studyDataSheet) {
      var startRow = startRows['Study design'];
      var epochColumnsByUri = {};
      var armRowsByUri = {};
      var epochHeaders = _.reduce(epochs, function(accum, epoch, index) {
        var epochTitleReference = IOU.getTitleReference(epochSheet, epoch.uri);
        var column = excelUtils.encode_col(index + 1);
        epochColumnsByUri[epoch.uri] = column;
        accum[column + (startRow + 1)] = IOU.cellFormula('Epochs!' + epochTitleReference);
        return accum;
      }, {});
      var armReferences = _.reduce(arms, function(accum, arm, index) {
        var row = startRow + 2 + index;
        var armCoordinates = _.map(_.range(0, arms.length), function(studyRow) {
          var datasheetRow = 3 + studyRow + startRows['Study data'];
          return excelUtils.encode_cell({
            r: datasheetRow,
            c: 10 // K column
          });
        });
        var armReference = _.findKey(_.pick(studyDataSheet, armCoordinates), ['v', arm.label]);
        armRowsByUri[arm.armURI] = row;
        accum['A' + row] = IOU.cellFormula('\'Study data\'!' + armReference);
        return accum;
      }, {});

      var activityReferences = _.reduce(studyDesign, function(accum, coordinate) {
        var activityTitleReference = IOU.getTitleReference(activitiesSheet, coordinate.activityUri);

        accum[epochColumnsByUri[coordinate.epochUri] + armRowsByUri[coordinate.armUri]] = IOU.cellFormula(
          'Activities!' + activityTitleReference);
        return accum;
      }, {});

      var cornerCell = {};
      cornerCell['A' + (1 + startRow)] = IOU.cellValue('arm');
      var studyDesignSheet = _.merge(cornerCell, epochHeaders, armReferences, activityReferences);
      var lastColumn = excelUtils.encode_col(1 + epochs.length);
      studyDesignSheet['!ref'] = 'A1:' + lastColumn + (startRow + arms.length + 1);

      return studyDesignSheet;
    }

    function buildMeasurementMomentSheet(startRows, measurementMoments, epochSheet) {
      var startRow = startRows['Measurement moments'];
      var fromTypes = {
        'ontology:anchorEpochStart': 'start',
        'ontology:anchorEpochEnd': 'end'
      };
      var measurementMomentHeaders = {
        A1: IOU.cellValue('id'),
        B1: IOU.cellValue('name'),
        C1: IOU.cellValue('epoch'),
        D1: IOU.cellValue('from'),
        E1: IOU.cellValue('offset')
      };

      var measurementMomentData = _.reduce(measurementMoments, function(accum, measurementMoment, index) {
        var row = startRow + index + 2;
        var epochTitleReference = IOU.getTitleReference(epochSheet, measurementMoment.epochUri);
        accum['A' + row] = IOU.cellValue(measurementMoment.uri);
        accum['B' + row] = IOU.cellValue(measurementMoment.label);
        accum['C' + row] = IOU.cellFormula('Epochs!' + epochTitleReference);
        accum['D' + row] = IOU.cellValue(fromTypes[measurementMoment.relativeToAnchor]);
        accum['E' + row] = IOU.cellValue(measurementMoment.offset);
        return accum;
      }, {});
      return _.merge(IOU.cellReference('A1:E' + (startRow + measurementMoments.length + 1)), measurementMomentHeaders, measurementMomentData);
    }

    function buildConceptsSheet(startRows, studyConcepts) {
      var startRow = startRows.Concepts;
      var conceptsHeaders = {
        A1: IOU.cellValue('id'),
        B1: IOU.cellValue('label'),
        C1: IOU.cellValue('type'),
        D1: IOU.cellValue('dataset concept uri'),
        E1: IOU.cellValue('multiplier')
      };

      var conceptsData = _.reduce(studyConcepts, function(accum, concept, index) {
        var row = startRow + index + 2;
        accum['A' + row] = IOU.cellValue(concept.uri);
        accum['B' + row] = IOU.cellValue(concept.label);
        accum['C' + row] = IOU.cellValue(concept.type);
        accum['D' + row] = concept.conceptMapping ? IOU.cellValue(concept.conceptMapping) : undefined;
        accum['E' + row] = IOU.cellNumber(concept.conversionMultiplier);
        return accum;
      }, {});
      var ref = IOU.cellReference('A1:E' + (studyConcepts.length + startRow + 1));
      return _.merge({}, ref, conceptsHeaders, conceptsData);
    }

    function buildDatasetInformationSheet(datasetWithCoordinates) {
      var datasetInfomationHeaders = {
        A1: IOU.cellValue('title'),
        B1: IOU.cellValue('ADDIS url'),
        C1: IOU.cellValue('description'),
      };
      var datasetInfomationData = {
        A2: IOU.cellValue(datasetWithCoordinates.title),
        B2: IOU.cellLink(datasetWithCoordinates.url),
        C2: IOU.cellValue(datasetWithCoordinates.comment)
      };
      return _.merge({
        '!ref': 'A1:C2'
      }, datasetInfomationHeaders, datasetInfomationData);
    }

    function buildDatasetConceptsSheet(datasetConcepts) {
      var datasetConceptsHeaders = {
        A1: IOU.cellValue('id'),
        B1: IOU.cellValue('label'),
        C1: IOU.cellValue('type')
      };
      var datasetConceptsData = _.reduce(datasetConcepts, function(accum, datasetConcept, index) {
        var row = index + 2;
        accum['A' + row] = IOU.cellValue(datasetConcept.uri);
        accum['B' + row] = IOU.cellValue(datasetConcept.label);
        accum['C' + row] = IOU.cellValue(datasetConcept.type.label);
        return accum;
      }, {});
      var ref = 'A1:C' + (datasetConcepts.length + 1);
      return _.merge({
        '!ref': ref
      }, datasetConceptsHeaders, datasetConceptsData);
    }

    function getStudyUrl(root, coordinates) {
      return root + '/users/' + coordinates.userUid + '/datasets/' +
        coordinates.datasetUuid + '/versions/' +
        coordinates.versionUuid + '/studies/' +
        coordinates.graphUuid;
    }

    function buildWorkBook() {
      var workBook = excelUtils.book_new();
      excelUtils.book_append_sheet(workBook, {}, 'Study data');
      excelUtils.book_append_sheet(workBook, {}, 'Activities');
      excelUtils.book_append_sheet(workBook, {}, 'Epochs');
      excelUtils.book_append_sheet(workBook, {}, 'Study design');
      excelUtils.book_append_sheet(workBook, {}, 'Measurement moments');
      excelUtils.book_append_sheet(workBook, {}, 'Concepts');
      return workBook;
    }

    function buildStartRows(offset) {
      return {
        'Study data': 0,
        Activities: offset,
        Epochs: offset,
        'Study design': offset,
        'Measurement moments': offset,
        Concepts: offset
      };
    }

    function updateStartRows(workBook) {
      return _.reduce(workBook.Sheets, function(accum, sheet, sheetName) {
        accum[sheetName] = nextStartRow(sheet, sheetName);
        return accum;
      }, {});
    }


    function addStudyHeaders(workBook, startRows) {
      var newWorkBook = _.cloneDeep(workBook);
      newWorkBook.Sheets = _.reduce(workBook.Sheets, function(accum, sheet, sheetName) {
        var newSheet = _.cloneDeep(sheet);
        if (sheetName !== 'Study data') {
          var location = 'A' + (startRows['Study data'] + 4);
          newSheet['A' + (startRows[sheetName] + (sheetName === 'Study design' ? 0 : 1))] = IOU.cellFormula('\'Study data\'!' + location);
        }
        accum[sheetName] = newSheet;
        return accum;
      }, {});
      return newWorkBook;
    }

    // ----------- utility functions ----------------
    function nextStartRow(sheet, sheetName) {
      var ref = excelUtils.decode_range(sheet['!ref']);
      var offset = 2;
      if (sheetName === 'Study data') {
        offset = 1;
      } else if (sheetName === 'Study design') {
        offset = 3;
      }
      return ref.e.r + offset;
    }

    function buildVariablesData(startRow, variables, arms, conceptsSheet, measurementMomentSheet) {
      var anchorCell = {
        c: excelUtils.decode_col('M'),
        r: 1 + startRow
      };
      var context = {
        arms: arms,
        conceptsSheet: conceptsSheet,
        measurementMomentSheet: measurementMomentSheet
      };
      var variableBlocks = _.flatten(_.map(variables, _.partial(buildVariableBlock, context)));
      var merges;
      var numberofArms = arms.length;
      merges = [];
      var currentAnchorCol = anchorCell.c;
      _.forEach(variables, function(variable) {
        // first two columns are always merged
        var firstDataRow = anchorCell.r + 2;
        merges.push(IOU.cellRange(currentAnchorCol, firstDataRow, currentAnchorCol, firstDataRow + numberofArms - 1));
        merges.push(IOU.cellRange(currentAnchorCol + 1, firstDataRow, currentAnchorCol + 1, firstDataRow + numberofArms - 1));

        var numberOfPropertyColumns = calcNumberOfPropertyColumns(variable);
        var numberOfMeasurementMoments = variable.measuredAtMoments.length;

        // for every measurement moment we merge the first column
        _.forEach(_.range(0, numberOfMeasurementMoments), function(index) {
          var positionOfMeasurementMoment = currentAnchorCol + 2 + ((numberOfPropertyColumns + 1) * index);
          merges.push(IOU.cellRange(positionOfMeasurementMoment, firstDataRow, positionOfMeasurementMoment, firstDataRow + numberofArms - 1));
        });
        var totalVariableLength = 2 + (numberOfPropertyColumns + 1) * numberOfMeasurementMoments;
        merges.push(IOU.cellRange(currentAnchorCol, anchorCell.r, currentAnchorCol + totalVariableLength - 1, anchorCell.r));
        currentAnchorCol = currentAnchorCol + totalVariableLength;
      });
      var variablesData = IOU.arrayToA1FromCoordinate(anchorCell.c, anchorCell.r, variableBlocks);
      return {
        data: variablesData,
        merges: merges
      };
    }

    function buildVariableBlock(context, variable) {
      // fixed block (not dependent on measured at moments)
      var measurementTypes = {
        'ontology:dichotomous': 'dichotomous',
        'ontology:continuous': 'continuous',
        'ontology:categorical': 'categorical',
        'ontology:survival': 'survival'
      };

      var variableReference = IOU.getTitleReference(context.conceptsSheet, variable.uri);
      var baseColumns = [
        [IOU.cellFormula('Concepts!' + variableReference), IOU.cellValue('variable type'), IOU.cellValue(variable.type)],
        [undefined, IOU.cellValue('measurement type'), IOU.cellValue(measurementTypes[variable.measurementType])]
      ];

      if (hasTimeScaleColumn(variable)) {
        baseColumns.push([undefined, IOU.cellValue('time scale'), IOU.cellValue(variable.timeScale)]);
      }
      var measuredAtContext = _.merge({
        variable: variable
      }, context);
      var measurementsBlocks = _.flatten(_.map(variable.measuredAtMoments, _.partial(buildMeasurementMomentBlocks, measuredAtContext)));
      if (measuredAtContext.variable && !measurementsBlocks.length) {
        var properties;
        if (measuredAtContext.variable.resultProperties) {
          properties = _.map(measuredAtContext.variable.resultProperties, function(item) {
            return item.split('#')[1];
          });
        } else {
          properties = _.map(measuredAtContext.variable.categoryList, 'label');
        }
        measurementsBlocks = _.map(properties, function(property) {
          return [undefined, IOU.cellValue(property)];
        });
      }
      return baseColumns.concat(measurementsBlocks);
    }

    function buildMeasurementMomentBlocks(context, measuredAtMoment) {
      var measurementMomentTitleReference = IOU.getTitleReference(context.measurementMomentSheet, measuredAtMoment.uri);
      var baseColumns = [
        [undefined, IOU.cellValue('measurement moment'), IOU.cellFormula('\'Measurement moments\'!' + measurementMomentTitleReference)]
      ];
      var measuredAtContext = _.merge({
        measurementMoment: measuredAtMoment
      }, context);
      var properties;
      if (context.variable.resultProperties) {
        measuredAtContext.labelExtractor = function(item) {
          return item.split('#')[1];
        };
        measuredAtContext.isResultForThisProperty = function(result, item) {
          return result.result_property === item.split('#')[1];
        };
        properties = context.variable.resultProperties;
      } else { // categorical
        measuredAtContext.labelExtractor = function(item) {
          return item.label;
        };
        measuredAtContext.isResultForThisProperty = function(result, item) {
          return result.result_property.category === item['@id'];
        };
        properties = context.variable.categoryList;
      }
      var resultColumns = _.map(properties, _.partial(buildResultColumn, measuredAtContext));
      return baseColumns.concat(resultColumns);
    }

    function buildResultColumn(context, resultProperty) {
      return [undefined, IOU.cellValue(context.labelExtractor(resultProperty))].concat(_.map(context.arms, function(arm) {
        var result = _.find(context.variable.results, function(result) {
          return result.momentUri === context.measurementMoment.uri &&
            result.armUri === arm.armURI &&
            context.isResultForThisProperty(result, resultProperty);
        });
        return result ? IOU.cellNumber(result.value) : undefined;
      }));
    }

    function initStudyDataSheet() {
      var studyDataSheet = {
        A1: IOU.cellValue('Study Information'),
        I1: IOU.cellValue('Population Information'),
        K1: IOU.cellValue('Arm Information'),
        M1: IOU.cellValue('Measurement Information')
      };
      studyDataSheet['!merges'] = [
        IOU.cellRange(0, 0, 7, 0), // Study Information header
        IOU.cellRange(8, 0, 9, 0), // Population information header
        IOU.cellRange(10, 0, 11, 0) // arm information header
      ];
      return studyDataSheet;
    }

    function buildStudyHeaders(startRow) {
      return IOU.arrayToA1FromCoordinate(0, startRow + 2, [
        [IOU.cellValue('id')],
        [IOU.cellValue('addis url')],
        [IOU.cellValue('title')],
        [IOU.cellValue('group allocation')],
        [IOU.cellValue('blinding')],
        [IOU.cellValue('status')],
        [IOU.cellValue('number of centers')],
        [IOU.cellValue('objective')],
        [IOU.cellValue('indication')],
        [IOU.cellValue('eligibility criteria')],
        [IOU.cellValue('title')],
        [IOU.cellValue('description')]
      ]);
    }

    function buildStudyInformation(startRow, study, studyInformation, studyUrl) {
      return IOU.arrayToA1FromCoordinate(0, startRow + 3, [
        [IOU.cellValue(study.label)],
        [IOU.cellLink(studyUrl)],
        [IOU.cellValue(study.comment)],
        [IOU.cellValue(studyInformation.allocation ? GROUP_ALLOCATION_OPTIONS[studyInformation.allocation].label : undefined)],
        [IOU.cellValue(studyInformation.blinding ? BLINDING_OPTIONS[studyInformation.blinding].label : undefined)],
        [IOU.cellValue(studyInformation.status ? STATUS_OPTIONS[studyInformation.status].label : undefined)],
        [IOU.cellNumber(studyInformation.numberOfCenters)],
        [IOU.cellValue(studyInformation.objective ? studyInformation.objective.comment : undefined)]
      ]);
    }

    function buildArmData(startRow, arms) {
      var overallPopulation = {
        label: 'Overall population'
      };
      return _.reduce(arms.concat(overallPopulation), function(acc, arm, idx) {
        var rowNum = startRow + 4 + idx;
        acc['K' + rowNum] = IOU.cellValue(arm.label);
        acc['L' + rowNum] = IOU.cellValue(arm.comment);
        return acc;
      }, {});
    }

    function buildArmMerges(startRow, arms) { // vertical merges for all study information across arms
      return _.map(_.range(0, 10), function(i) {
        return IOU.cellRange(i, startRow + 3, i, startRow + 3 + arms.length - 1);
      });
    }

    function buildPopulationInformationData(startRow, populationInformation) {
      var populationInformationData = {};
      populationInformationData['I' + (startRow + 4)] = IOU.cellValue(populationInformation.indication ? populationInformation.indication.label : undefined);
      populationInformationData['J' + (startRow + 4)] = IOU.cellValue(populationInformation.eligibilityCriteria ? populationInformation.eligibilityCriteria.label : undefined);
      return populationInformationData;
    }

    function calcNumberOfPropertyColumns(variable) {
      var numberOfColumns = variable.categoryList ? variable.categoryList.length : variable.resultProperties.length;
      if (hasTimeScaleColumn(variable)) {
        ++numberOfColumns;
      }
      return numberOfColumns;
    }

    function hasTimeScaleColumn(variable) {
      return !!variable.timeScale;
    }

    function addConceptType(concepts, type) {
      return _.map(concepts, function(concept) {
        return _.merge({}, concept, {
          type: type
        });
      });
    }

    // interface
    return {
      getVariableResults: getVariableResults,
      mergePreservingRange: mergePreservingRange,
      buildConceptsSheet: buildConceptsSheet,
      buildEpochSheet: buildEpochSheet,
      buildMeasurementMomentSheet: buildMeasurementMomentSheet,
      buildStudyDataSheet: buildStudyDataSheet,
      buildActivitiesSheet: buildActivitiesSheet,
      buildStudyDesignSheet: buildStudyDesignSheet,
      buildDatasetInformationSheet: buildDatasetInformationSheet,
      buildDatasetConceptsSheet: buildDatasetConceptsSheet,
      addConceptType: addConceptType,
      addStudyHeaders: addStudyHeaders,
      getStudyUrl: getStudyUrl,
      buildWorkBook: buildWorkBook,
      buildStartRows: buildStartRows,
      updateStartRows: updateStartRows
    };

  };
  return dependencies.concat(ExcelExportService);
});
