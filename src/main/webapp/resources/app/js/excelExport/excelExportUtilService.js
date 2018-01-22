'use strict';
define(['lodash', 'xlsx-shim'], function(_, XLSX) {
  var dependencies = ['$q', '$location', 'GROUP_ALLOCATION_OPTIONS', 'BLINDING_OPTIONS', 'STATUS_OPTIONS', 'ResultsService'];
  var ExcelExportService = function($q, $location, GROUP_ALLOCATION_OPTIONS, BLINDING_OPTIONS, STATUS_OPTIONS, ResultsService) {
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

    function buildStudyDataSheet(study, studyInformation, arms, epochs, activities, studyDesign,
      populationInformation, variables, conceptsSheet, measurementMomentSheet) {
      var studyDataSheet = initStudyDataSheet();
      var initialMerges = [
        cellRange(0, 0, 7, 0),
        cellRange(8, 0, 9, 0),
        cellRange(10, 0, 12, 0)
      ];
      var studyData = buildStudyInformation(study, studyInformation);
      var armData = buildArmData(arms);
      var treatmentLabels = buildTreatmentLabels(arms, epochs, activities, studyDesign);
      var populationInformationData = buildPopulationInformationData(populationInformation);
      var variablesData = buildVariablesData(variables, arms, conceptsSheet, measurementMomentSheet);
      var armMerges = buildArmMerges(arms);

      _.merge(studyDataSheet, studyData, populationInformationData, armData, treatmentLabels, variablesData);
      studyDataSheet['!merges'] = initialMerges.concat(armMerges, studyDataSheet['!merges']);
      var measurementCounter = 30; // placeholder FIXME once measurements are implemented
      studyDataSheet['!ref'] = 'A1:' +
        excelUtils.encode_col(11 + measurementCounter) +
        (3 + arms.length);
      return studyDataSheet;
    }

    function buildVariablesData(variables, arms, conceptsSheet, measurementMomentSheet) {
      var column = excelUtils.decode_col('N');
      var headerRow = 3;
      var variablesData = _.reduce(variables, function(accum, variable) {
        if (!variable.resultProperties) {
          return accum;
        } // FIXME: support categoricals

        var numberOfColumns = 3 + variable.measuredAtMoments.length + variable.resultProperties.length;

        var variableReference = getTitleReference(conceptsSheet, variable.uri);

        // fixed block (not dependent on measured at moments)
        var titleAddress = excelUtils.encode_col(column) + 2;
        accum[titleAddress] = cellFormula('=Concepts!' + variableReference);
        accum[excelUtils.encode_col(column) + headerRow] = cellValue('id');
        accum[excelUtils.encode_col(column + 1) + headerRow] = cellValue('variable type');
        accum[excelUtils.encode_col(column + 2) + headerRow] = cellValue('measurement type');
        accum[excelUtils.encode_col(column) + (headerRow + 1)] = cellValue(variable.uri);
        accum[excelUtils.encode_col(column + 1) + (headerRow + 1)] = cellValue(variable.type);
        accum[excelUtils.encode_col(column + 2) + (headerRow + 1)] = cellValue(variable.measurementType);
        accum['!merges'] = accum['!merges'].concat(_.map(_.range(0, 3), function(i) {
          return cellRange(column + i, headerRow, column + i, headerRow + arms.length - 1);
        }));

        // FIXME: values + vertical merges

        // variable block (dependent on measured at moments + result properties)
        var measurementsBlock = _.reduce(variable.measuredAtMoments, function(measuredAtMomentAccum, measuredAtMoment, index) {
          var measurementMomentCell = getTitleReference(measurementMomentSheet, measuredAtMoment.uri);
          var dataStartColumn = column + 3 + index * (variable.resultProperties.length + 1);
          measuredAtMomentAccum[excelUtils.encode_col(dataStartColumn) + headerRow] = cellValue('measurement moment');
          measuredAtMomentAccum[excelUtils.encode_col(dataStartColumn) + (headerRow + 1)] = cellFormula('=\'Measurement moments\'!' + measurementMomentCell);
          return measuredAtMomentAccum;
        }, {});

        var mergeEnd = excelUtils.decode_cell(titleAddress);
        mergeEnd.c += numberOfColumns - 1;
        accum['!merges'].push({
          s: excelUtils.decode_cell(titleAddress),
          e: mergeEnd
        });
        column += numberOfColumns;
        return _.merge(accum, measurementsBlock);
      }, {
        '!merges': []
      });
      return variablesData;
    }

    function initStudyDataSheet() {
      var studyDataSheet = {
        A1: cellValue('Study Information'),
        I1: cellValue('Population Information'),
        K1: cellValue('Arm Information'),
        N1: cellValue('Measurement Information'),
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
        L3: cellValue('description'),
        M3: cellValue('treatment')
      };
      return studyDataSheet;
    }

    function buildStudyInformation(study, studyInformation) {
      return {
        A4: cellValue(study.label),
        B4: cellValue($location.absUrl()),
        C4: cellValue(study.comment),
        D4: cellValue(GROUP_ALLOCATION_OPTIONS[studyInformation.allocation].label),
        E4: cellValue(BLINDING_OPTIONS[studyInformation.blinding].label),
        F4: cellValue(STATUS_OPTIONS[studyInformation.status].label),
        G4: cellValue(studyInformation.numberOfCenters),
        H4: cellValue(studyInformation.objective.comment)
      };
    }

    function buildArmData(arms) {
      return _.reduce(arms, function(acc, arm, idx) {
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
        I4: cellValue(populationInformation.indication.label),
        J4: cellValue(populationInformation.eligibilityCriteria.label)
      };
    }

    function buildTreatmentLabels(arms, epochs, activities, studyDesign) {
      var primaryEpoch = _.find(epochs, 'isPrimary');
      if (!primaryEpoch) {
        return;
      }
      return _.reduce(arms, function(acc, arm, idx) {
        var activity = _.find(activities, function(activity) {
          var coordinate = _.find(studyDesign, function(coordinate) {
            return coordinate.epochUri === primaryEpoch.uri && coordinate.armUri === arm.armURI;
          });
          return coordinate && coordinate.activityUri === activity.activityUri;
        });

        if (activity) {
          acc['M' + (4 + idx)] = {
            v: activity.label
          };

        }
        return acc;
      }, {});
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
        accum[excelUtils.encode_col(4 + i * 6) + 1] = cellValue('drug label');
        accum[excelUtils.encode_col(5 + i * 6) + 1] = cellValue('dose type');
        accum[excelUtils.encode_col(6 + i * 6) + 1] = cellValue('dose');
        accum[excelUtils.encode_col(7 + i * 6) + 1] = cellValue('max dose');
        accum[excelUtils.encode_col(8 + i * 6) + 1] = cellValue('unit');
        accum[excelUtils.encode_col(9 + i * 6) + 1] = cellValue('periodicity');
        return accum;
      }, {});

      var activityData = _.reduce(activities, function(accum, activity, index) {
        var row = index + 2;
        accum[excelUtils.encode_col(0) + row] = cellValue(activity.activityUri);
        accum[excelUtils.encode_col(1) + row] = cellValue(activity.label);
        accum[excelUtils.encode_col(2) + row] = cellValue(activity.activityType.label);
        accum[excelUtils.encode_col(3) + row] = cellValue(activity.activityDescription);

        if (activity.activityType.uri === 'ontology:TreatmentActivity') {
          _.forEach(activity.treatments, function(treatment, index) {
            var isFixedDose = treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment';
            accum[excelUtils.encode_col(4 + index * 6) + row] = cellFormula('=Concepts!' + _.findKey(conceptsSheet, ['v', treatment.drug.label]));
            accum[excelUtils.encode_col(5 + index * 6) + row] = cellValue(doseTypes[treatment.treatmentDoseType]);
            accum[excelUtils.encode_col(6 + index * 6) + row] = cellValue(isFixedDose ? treatment.fixedValue : treatment.minValue);
            accum[excelUtils.encode_col(7 + index * 6) + row] = cellValue(isFixedDose ? undefined : treatment.maxValue);
            accum[excelUtils.encode_col(8 + index * 6) + row] = cellFormula('=Concepts!' + getTitleReference(conceptsSheet, treatment.doseUnit.uri));
            accum[excelUtils.encode_col(9 + index * 6) + row] = cellValue(treatment.dosingPeriodicity);
          });
        }
        return accum;
      }, {});

      return _.merge(cellReference('A1:' + excelUtils.encode_col(4 + maxTreatments * 6) + '' + (activities.length + 1)), sheet, colHeaders, activityData);
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

    // interface
    return {
      getVariableResults: getVariableResults,
      buildConceptsSheet: buildConceptsSheet,
      buildEpochSheet: buildEpochSheet,
      buildMeasurementMomentSheet: buildMeasurementMomentSheet,
      buildStudyDataSheet: buildStudyDataSheet,
      buildActivitiesSheet: buildActivitiesSheet,
      buildStudyDesignSheet: buildStudyDesignSheet,
      addConceptType: addConceptType
    };

  };
  return dependencies.concat(ExcelExportService);
});