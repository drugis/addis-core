'use strict';
define(['lodash', 'xlsx-shim'], function(_, XLSX) {
  var dependencies = ['$q',
    'GROUP_ALLOCATION_OPTIONS',
    'BLINDING_OPTIONS',
    'STATUS_OPTIONS',
    'ResultsService'
  ];
  var ExcelExportService = function($q,
    GROUP_ALLOCATION_OPTIONS,
    BLINDING_OPTIONS,
    STATUS_OPTIONS,
    ResultsService
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

    // build sheets
    function buildStudyDataSheet(study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
      populationInformation, variables, conceptsSheet, measurementMomentSheet) {
      var studyDataSheet = initStudyDataSheet();
      var initialMerges = [
        cellRange(0, 0, 7, 0),
        cellRange(8, 0, 9, 0),
        cellRange(10, 0, 11, 0)
      ];
      var armsPlusOverallPopulation = arms.concat({
        armURI: study.has_included_population[0]['@id']
      });
      var studyData = buildStudyInformation(study, studyInformation, studyUrl);
      var armData = buildArmData(arms);
      // var treatmentLabels = buildTreatmentLabels(arms, epochs, activities, studyDesign);
      var populationInformationData = buildPopulationInformationData(populationInformation);
      var variablesData = buildVariablesData(variables, armsPlusOverallPopulation, conceptsSheet, measurementMomentSheet);
      var armMerges = buildArmMerges(armsPlusOverallPopulation);

      _.merge(studyDataSheet, studyData, populationInformationData, armData, variablesData);
      var lastColumn = variables.length ? _(variablesData)
        .keys()
        .map(excelUtils.decode_cell)
        .map('c')
        .max() : 12;

      studyDataSheet['!merges'] = studyDataSheet['!merges'].concat(initialMerges, [cellRange(12, 0, lastColumn, 0)], armMerges);
      studyDataSheet['!ref'] = 'A1:' + a1Coordinate(lastColumn, 3 + arms.length);
      return studyDataSheet;
    }

    function buildActivitiesSheet(activities, conceptsSheet) {
      var doseTypes = {
        'ontology:FixedDoseDrugTreatment': 'fixed',
        'ontology:TitratedDoseDrugTreatment': 'titrated'
      };

      var sheet = {
        A1: cellValue('id'),
        B1: cellValue('title'),
        C1: cellValue('type'),
        D1: cellValue('description')
      };

      var maxTreatments = _.max(_.map(activities, function(activity) {
        return activity.treatments ? activity.treatments.length : 0;
      }));
      var colHeaders = _.reduce(_.range(0, maxTreatments), function(accum, i) {
        accum[a1Coordinate(4 + i * 6, 0)] = cellValue('drug label');
        accum[a1Coordinate(5 + i * 6, 0)] = cellValue('dose type');
        accum[a1Coordinate(6 + i * 6, 0)] = cellValue('dose');
        accum[a1Coordinate(7 + i * 6, 0)] = cellValue('max dose');
        accum[a1Coordinate(8 + i * 6, 0)] = cellValue('unit');
        accum[a1Coordinate(9 + i * 6, 0)] = cellValue('periodicity');
        return accum;
      }, {});

      var activityData = _.reduce(activities, function(accum, activity, index) {
        var row = index + 1;
        accum[a1Coordinate(0, row)] = cellValue(activity.activityUri);
        accum[a1Coordinate(1, row)] = cellValue(activity.label);
        accum[a1Coordinate(2, row)] = cellValue(activity.activityType.label);
        accum[a1Coordinate(3, row)] = cellValue(activity.activityDescription);

        if (activity.activityType.uri === 'ontology:TreatmentActivity') {
          _.forEach(activity.treatments, function(treatment, index) {
            var isFixedDose = treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment';
            accum[a1Coordinate(4 + index * 6, row)] = cellFormula('=Concepts!' + _.findKey(conceptsSheet, ['v', treatment.drug.label]));
            accum[a1Coordinate(5 + index * 6, row)] = cellValue(doseTypes[treatment.treatmentDoseType]);
            accum[a1Coordinate(6 + index * 6, row)] = cellValue(isFixedDose ? treatment.fixedValue : treatment.minValue);
            accum[a1Coordinate(7 + index * 6, row)] = cellValue(isFixedDose ? undefined : treatment.maxValue);
            accum[a1Coordinate(8 + index * 6, row)] = cellFormula('=Concepts!' + getTitleReference(conceptsSheet, treatment.doseUnit.uri));
            accum[a1Coordinate(9 + index * 6, row)] = cellValue(treatment.dosingPeriodicity);
          });
        }
        return accum;
      }, {});

      return _.merge(cellReference('A1:' + a1Coordinate(4 + maxTreatments * 6, activities.length)), sheet, colHeaders, activityData);
    }

    function buildEpochSheet(epochs) {
      var epochHeaders = {
        A1: cellValue('id'),
        B1: cellValue('name'),
        C1: cellValue('description'),
        D1: cellValue('duration'),
        E1: cellValue('Is primary?')
      };
      var epochData = _.reduce(epochs, function(accum, epoch, index) {
        var row = index + 2;
        accum['A' + row] = cellValue(epoch.uri);
        accum['B' + row] = cellValue(epoch.label);
        accum['C' + row] = cellValue(epoch.comment);
        accum['D' + row] = cellValue(epoch.duration);
        accum['E' + row] = cellValue(epoch.isPrimary);
        return accum;
      }, {});

      return _.merge(cellReference('A1:E' + (epochs.length + 1)), epochHeaders, epochData);
    }

    function buildStudyDesignSheet(epochs, arms, studyDesign, epochSheet, activitiesSheet, studyDataSheet) {
      var epochColumnsByUri = {};
      var armRowsByUri = {};
      var epochHeaders = _.reduce(epochs, function(accum, epoch, index) {
        var epochTitleReference = getTitleReference(epochSheet, epoch.uri);
        var column = excelUtils.encode_col(index + 1);
        epochColumnsByUri[epoch.uri] = column;
        accum[column + 1] = cellFormula('=Epochs!' + epochTitleReference);
        return accum;
      }, {});
      var armReferences = _.reduce(arms, function(accum, arm, index) {
        var row = 2 + index;
        var armReference = _.findKey(studyDataSheet, ['v', arm.label]);
        armRowsByUri[arm.armURI] = row;
        accum['A' + row] = cellFormula('=\'Study Data\'!' + armReference);
        return accum;
      }, {});

      var activityReferences = _.reduce(studyDesign, function(accum, coordinate) {
        var activityTitleReference = getTitleReference(activitiesSheet, coordinate.activityUri);

        accum[epochColumnsByUri[coordinate.epochUri] + armRowsByUri[coordinate.armUri]] = cellFormula('=Activities!' + activityTitleReference);
        return accum;
      }, {});

      var studyDesignSheet = _.merge({
          A1: cellValue('arm')
        },
        epochHeaders, armReferences, activityReferences);
      var lastColumn = excelUtils.encode_col(1 + epochs.length);
      studyDesignSheet['!ref'] = 'A1:' + lastColumn + (arms.length + 1);

      return studyDesignSheet;
    }

    function buildMeasurementMomentSheet(measurementMoments, epochSheet) {
      var fromTypes = {
        'ontology:anchorEpochStart': 'start',
        'ontology:anchorEpochEnd': 'end'
      };
      var measurementMomentHeaders = {
        A1: cellValue('id'),
        B1: cellValue('name'),
        C1: cellValue('epoch'),
        D1: cellValue('from'),
        E1: cellValue('offset')
      };

      var measurementMomentData = _.reduce(measurementMoments, function(accum, measurementMoment, index) {
        var row = index + 2;
        accum['A' + row] = cellValue(measurementMoment.uri);
        accum['B' + row] = cellValue(measurementMoment.label);
        accum['C' + row] = cellFormula('=Epochs!' + _.findKey(epochSheet, ['v', measurementMoment.epochUri]));
        accum['D' + row] = cellValue(fromTypes[measurementMoment.relativeToAnchor]);
        accum['E' + row] = cellValue(measurementMoment.offset);
        return accum;
      }, {});
      return _.merge(cellReference('A1:E' + (measurementMoments.length + 1)), measurementMomentHeaders, measurementMomentData);
    }

    function buildConceptsSheet(studyConcepts) {
      var conceptsHeaders = {
        A1: cellValue('id'),
        B1: cellValue('label'),
        C1: cellValue('type'),
        D1: cellValue('dataset concept uri'),
        E1: cellValue('multiplier')
      };

      var conceptsData = _.reduce(studyConcepts, function(accum, concept, index) {
        var row = index + 2;
        accum['A' + row] = cellValue(concept.uri);
        accum['B' + row] = cellValue(concept.label);
        accum['C' + row] = cellValue(concept.type);
        accum['D' + row] = cellValue(concept.conceptMapping);
        accum['E' + row] = cellValue(concept.conversionMultiplier);
        return accum;
      }, {});
      var ref = cellReference('A1:E' + (studyConcepts.length + 1));
      return _.merge({}, ref, conceptsHeaders, conceptsData);
    }

    // assist functions
    function buildVariablesData(variables, arms, conceptsSheet, measurementMomentSheet) {
      var anchorCell = {
        c: excelUtils.decode_col('M'),
        r: 1
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
        merges.push(cellRange(currentAnchorCol, firstDataRow, currentAnchorCol, firstDataRow + numberofArms - 1));
        merges.push(cellRange(currentAnchorCol + 1, firstDataRow, currentAnchorCol + 1, firstDataRow + numberofArms - 1));

        var numberOfPropertyColumns = variable.categoryList ? variable.categoryList.length : variable.resultProperties.length;
        var numberOfMeasurementMoments = variable.measuredAtMoments.length;

        // for every measurement moment we merge the first column
        _.forEach(_.range(0, numberOfMeasurementMoments), function(index) {
          var positionOfMeasurementMoment = currentAnchorCol + 2 + ((numberOfPropertyColumns + 1) * index);
          merges.push(cellRange(positionOfMeasurementMoment, firstDataRow, positionOfMeasurementMoment, firstDataRow + numberofArms - 1));
        });
        var totalVariableLength = 2 + (numberOfPropertyColumns + 1) * numberOfMeasurementMoments;
        merges.push(cellRange(currentAnchorCol, anchorCell.r, currentAnchorCol + totalVariableLength - 1, anchorCell.r));
        currentAnchorCol = currentAnchorCol + totalVariableLength;
      });
      var variablesData = arrayToA1FromCoordinate(anchorCell.c, anchorCell.r, variableBlocks);
      variablesData['!merges'] = merges;
      return variablesData;
    }

    function buildVariableBlock(context, variable) {
      // fixed block (not dependent on measured at moments)
      var measurementTypes = {
        'ontology:dichotomous': 'dichotomous',
        'ontology:continuous': 'continuous',
        'ontology:categorical': 'categorical',
        'ontology:survival': 'survival'
      };

      var variableReference = getTitleReference(context.conceptsSheet, variable.uri);
      var baseColumns = [
        [cellFormula('=Concepts!' + variableReference), cellValue('variable type'), cellValue(variable.type)],
        [undefined, cellValue('measurement type'), cellValue(measurementTypes[variable.measurementType])]
      ];
      var measuredAtContext = _.merge({
        variable: variable
      }, context);
      var measurementsBlocks = _.flatten(_.map(variable.measuredAtMoments, _.partial(buildMeasurementMomentBlocks, measuredAtContext)));

      return baseColumns.concat(measurementsBlocks);
    }

    function buildMeasurementMomentBlocks(context, measuredAtMoment) {
      var measurementMomentTitleCoordinate = getTitleReference(context.measurementMomentSheet, measuredAtMoment.uri);
      // FIXME accum['!merges'].push(cellRange(column, row + 1, column, row + context.arms.length));
      var baseColumns = [
        [undefined, cellValue('measurement moment'), cellFormula('=\'Measurement moments\'!' + measurementMomentTitleCoordinate)]
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
      } else {
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
      return [undefined, cellValue(context.labelExtractor(resultProperty))].concat(_.map(context.arms, function(arm) {
        var result = _.find(context.variable.results, function(result) {
          return result.momentUri === context.measurementMoment.uri &&
            result.armUri === arm.armURI &&
            context.isResultForThisProperty(result, resultProperty);
        });
        return result ? cellValue(result.value) : undefined;
      }));
    }

    function initStudyDataSheet() {
      var studyDataSheet = {
        A1: cellValue('Study Information'),
        I1: cellValue('Population Information'),
        K1: cellValue('Arm Information'),
        M1: cellValue('Measurement Information'),
        A3: cellValue('id'),
        B3: cellValue('addis url'),
        C3: cellValue('title'),
        D3: cellValue('group allocation'),
        E3: cellValue('blinding'),
        F3: cellValue('status'),
        G3: cellValue('number of centers'),
        H3: cellValue('objective'),
        I3: cellValue('indication'),
        J3: cellValue('eligibility criteria'),
        K3: cellValue('title'),
        L3: cellValue('description')
      };
      return studyDataSheet;
    }

    function buildStudyInformation(study, studyInformation, studyUrl) {
      return {
        A4: cellValue(study.label),
        B4: {
          l: {
            Target: studyUrl
          },
          v: studyUrl
        },
        C4: cellValue(study.comment),
        D4: cellValue(studyInformation.allocation ? GROUP_ALLOCATION_OPTIONS[studyInformation.allocation].label : undefined),
        E4: cellValue(studyInformation.blinding ? BLINDING_OPTIONS[studyInformation.blinding].label : undefined),
        F4: cellValue(studyInformation.status ? STATUS_OPTIONS[studyInformation.status].label : undefined),
        G4: cellValue(studyInformation.numberOfCenters),
        H4: cellValue(studyInformation.objective ? studyInformation.objective.comment : undefined)
      };
    }

    function buildArmData(arms) {
      var overallPopulation = {
        label: 'Overall population'
      };
      return _.reduce(arms.concat(overallPopulation), function(acc, arm, idx) {
        var rowNum = (4 + idx);
        acc['K' + rowNum] = cellValue(arm.label);
        acc['L' + rowNum] = cellValue(arm.comment);
        return acc;
      }, {});
    }

    function buildArmMerges(arms) { // vertical merges for all study information across arms
      return _.map(_.range(0, 10), function(i) {
        return cellRange(i, 3, i, 3 + arms.length - 1);
      });
    }

    function buildPopulationInformationData(populationInformation) {
      return {
        I4: cellValue(populationInformation.indication ? populationInformation.indication.label : undefined),
        J4: cellValue(populationInformation.eligibilityCriteria ? populationInformation.eligibilityCriteria.label : undefined)
      };
    }

    function addConceptType(concepts, type) {
      return _.map(concepts, function(concept) {
        return _.merge({}, concept, {
          type: type
        });
      });
    }

    function getTitleReference(sheet, uri) {
      var uriReference = _.findKey(sheet, ['v', uri]);
      var titleReference = excelUtils.decode_cell(uriReference);
      titleReference.c += 1;
      return excelUtils.encode_cell(titleReference);
    }

    function cellValue(value) {
      return {
        v: value
      };
    }

    function cellFormula(formula) {
      return {
        f: formula
      };
    }

    function cellReference(reference) {
      return {
        '!ref': reference
      };
    }

    function cellRange(startCol, startRow, endCol, endRow) {
      return {
        s: {
          c: startCol,
          r: startRow
        },
        e: {
          c: endCol,
          r: endRow
        }
      };
    }

    function a1Coordinate(column, row) {
      return excelUtils.encode_cell({
        c: column,
        r: row
      });
    }

    /*
     * Create a A1-indexed object from the two-dimensional data-array.
     * Columns will be created from the first index, rows from the second.
     */
    function arrayToA1FromCoordinate(anchorColumn, anchorRow, data) {
      return _.reduce(data, function(accum, column, colIndex) {
        return _.reduce(column, function(accum, cell, rowIndex) {
          accum[a1Coordinate(anchorColumn + colIndex, anchorRow + rowIndex)] = cell;
          return accum;
        }, accum);
      }, {});
    }

    // interface
    return {
      getVariableResults: getVariableResults,
      buildConceptsSheet: buildConceptsSheet,
      buildEpochSheet: buildEpochSheet,
      buildMeasurementMomentSheet: buildMeasurementMomentSheet,
      buildStudyDataSheet: buildStudyDataSheet,
      buildActivitiesSheet: buildActivitiesSheet,
      buildStudyDesignSheet: buildStudyDesignSheet,
      addConceptType: addConceptType,
      arrayToA1FromCoordinate: arrayToA1FromCoordinate
    };

  };
  return dependencies.concat(ExcelExportService);
});