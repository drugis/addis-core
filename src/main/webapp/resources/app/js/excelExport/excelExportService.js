'use strict';
define(['lodash', 'xlsx-shim', 'file-saver'], function(_, XLSX, saveAs) {
  var dependencies = ['$q', 'GROUP_ALLOCATION_OPTIONS', 'BLINDING_OPTIONS', 'STATUS_OPTIONS',
    'StudyService',
    'ArmService',
    'StudyInformationService',
    'PopulationInformationService',
    'EpochService',
    'ActivityService',
    'StudyDesignService',
    'MeasurementMomentService',
    'ConceptsService'
  ];
  var ExcelExportService = function($q, GROUP_ALLOCATION_OPTIONS, BLINDING_OPTIONS, STATUS_OPTIONS,
    StudyService,
    ArmService,
    StudyInformationService,
    PopulationInformationService,
    EpochService,
    ActivityService,
    StudyDesignService,
    MeasurementMomentService,
    ConceptsService
  ) {

    function exportStudy() {
      var promises = [StudyService.getJsonGraph()];
      promises = promises.concat(_.map([
        ArmService,
        StudyInformationService,
        PopulationInformationService,
        EpochService,
        ActivityService,
        StudyDesignService,
        MeasurementMomentService,
        ConceptsService
      ], function(service) {
        return service.queryItems();
      }));

      return $q.all(promises).then(function(results) {

        var study = StudyService.findStudyNode(results[0]);
        var arms = results[1];
        var studyInformation = results[2][0];
        var populationInformation = results[3][0];
        var epochs = results[4];
        var activities = results[5];
        var studyDesign = results[6];
        var measurementMoments = results[7];
        var concepts = results[8];

        var workBook = XLSX.utils.book_new();

        var studyDataSheet = buildStudyDataSheet(study, studyInformation, arms, epochs, activities, studyDesign, populationInformation);
        var activitiesSheet = buildActivitiesSheet(activities);
        var epochSheet = buildEpochSheet(epochs);
        var studyDesignSheet = buildStudyDesignSheet(studyDesign, epochs, epochSheet, arms, studyDataSheet);
        var measurementMomentSheet = buildMeasurementMomentSheet(measurementMoments, epochSheet);
        var conceptSheet = buildConceptsSheet(concepts);

        XLSX.utils.book_append_sheet(workBook, studyDataSheet, 'Study data');
        XLSX.utils.book_append_sheet(workBook, activitiesSheet, 'Activities');
        XLSX.utils.book_append_sheet(workBook, epochSheet, 'Epochs');
        XLSX.utils.book_append_sheet(workBook, studyDesignSheet, 'Study design');
        XLSX.utils.book_append_sheet(workBook, measurementMomentSheet, 'Measurement moments');
        XLSX.utils.book_append_sheet(workBook, conceptSheet, 'Concepts');
        var workBookout = XLSX.write(workBook, {
          bookType: 'xlsx',
          type: 'array'
        });
        saveAs(new Blob([workBookout], {
          type: 'application/octet-stream'
        }), study.label + '.xlsx');
      });
    }

    //private
    function buildConceptsSheet(concepts) {
      var conceptsHeaders = {
        A1: {
          v: 'id'
        },
        B1: {
          v: 'label'
        },
        C1: {
          v: 'type'
        },
        D1: {
          v: 'dataset concept uri'
        },
        E1: {
          v: 'multiplier'
        }
      };
      var conceptsData = _.reduce(concepts, function(accum, concept, index){
        var row = index+2
        accum['A'+row];

        return accum;
      },{});
    }

    function buildMeasurementMomentSheet(measurementMoments, epochSheet) {
      var fromTypes = {
        'ontology:anchorEpochStart': 'start',
        'ontology:anchorEpochEnd': 'end'
      };
      var measurementMomentHeaders = {
        A1: {
          v: 'id'
        },
        B1: {
          v: 'name'
        },
        C1: {
          v: 'epoch'
        },
        D1: {
          v: 'from'
        },
        E1: {
          v: 'offset'
        }
      };
      var measurementMomentData = _.reduce(measurementMoments, function(accum, measurementMoment, index) {
        var row = index + 2;
        accum['A' + row] = {
          v: measurementMoment.uri
        };
        accum['B' + row] = {
          v: measurementMoment.label
        };
        accum['C' + row] = {
          f: '=Epochs!' + _.findKey(epochSheet, function(epochCell) {
            return epochCell.v === measurementMoment.epochUri;
          })
        };
        accum['D' + row] = {
          v: fromTypes[measurementMoment.relativeToAnchor]
        };
        accum['E' + row] = {
          v: measurementMoment.offset
        };
        return accum;
      }, {});
      return _.merge({
        '!ref': 'A1:E' + (measurementMoments.length + 1)
      }, measurementMomentHeaders, measurementMomentData);
    }

    function buildStudyDesignSheet(studyDesign, epochs, epochSheet, arms, studyDataSheet) {

      var studyDesignSheet = _.merge({}, {
        A1: {
          v: 'arm'
        }
      }, _.reduce(epochs, function(accum, epoch, index) {
        var epochReference = _.findKey(epochSheet, function(epochCell) {
          return epochCell.v === epoch.label;
        });
        var column = XLSX.utils.encode_col(index + 1);
        accum[column + 1] = {
          f: '=Epochs!' + epochReference
        };
        return accum;
      }, {}));
      var lastColumn = XLSX.utils.encode_col(1 + epochs.length);
      studyDesignSheet['!ref'] = 'A1:' + lastColumn + arms.length;
      return studyDesignSheet;
    }

    function buildEpochSheet(epochs) {
      var epochHeaders = {
        A1: {
          v: 'id'
        },
        B1: {
          v: 'name'
        },
        C1: {
          v: 'description'
        },
        D1: {
          v: 'duration'
        },
        E1: {
          v: 'Is primary?'
        }
      };
      var epochData = _.reduce(epochs, function(accum, epoch, index) {
        var row = index + 2;
        accum['A' + row] = {
          v: epoch.uri
        };
        accum['B' + row] = {
          v: epoch.label
        };
        accum['C' + row] = {
          v: epoch.comment
        };
        accum['D' + row] = {
          v: epoch.duration
        };
        accum['E' + row] = {
          v: epoch.isPrimary
        };
        return accum;
      }, {});

      return _.merge({
        '!ref': 'A1:E' + (epochs.length + 1)
      }, epochHeaders, epochData);
    }

    function buildStudyDataSheet(study, studyInformation, arms, epochs, activities, studyDesign, populationInformation) {
      var studyDataSheet = initStudyDataSheet();
      var studyData = buildStudyInformation(study, studyInformation);
      var armData = buildArmData(arms);
      var treatmentLabels = buildTreatmentLabels(arms, epochs, activities, studyDesign);
      var populationInformationData = buildPopulationInformationData(populationInformation);

      var armMerges = buildArmMerges(arms);

      _.merge(studyDataSheet, studyData, populationInformationData, armData, treatmentLabels);
      studyDataSheet['!merges'] = studyDataSheet['!merges'].concat(armMerges);
      var measurementCounter = 30; // placeholder FIXME once measurements are implemented
      studyDataSheet['!ref'] = 'A1:' +
        XLSX.utils.encode_col(11 + measurementCounter) +
        (3 + arms.length);
      return studyDataSheet;
    }

    function initStudyDataSheet() {
      var studyDataSheet = {
        A1: { //row 1
          v: 'Study Information'
        },
        I1: {
          v: 'Population Information'
        },
        K1: {
          v: 'Arm Information'
        },
        N1: {
          v: 'Measurement Information'
        },
        A3: { //row 3
          v: 'id'
        },
        B3: {
          v: 'addis url'
        },
        C3: {
          v: 'title'
        },
        D3: {
          v: 'group allocation'
        },
        E3: {
          v: 'blinding'
        },
        F3: {
          v: 'status'
        },
        G3: {
          v: 'number of centers'
        },
        H3: {
          v: 'objective'
        },
        I3: {
          v: 'indication'
        },
        J3: {
          v: 'eligibility criteria'
        },
        K3: {
          v: 'title'
        },
        L3: {
          v: 'description'
        },
        M3: {
          v: 'treatment'
        }
      };
      studyDataSheet['!merges'] = [{
        s: {
          c: 0,
          r: 0
        },
        e: {
          c: 7,
          r: 0
        }
      }, {
        s: {
          c: 8,
          r: 0
        },
        e: {
          c: 9,
          r: 0
        }
      }, {
        s: {
          c: 10,
          r: 0
        },
        e: {
          c: 12,
          r: 0
        }
      }];
      return studyDataSheet;
    }

    function buildStudyInformation(study, studyInformation) {
      return {
        A4: {
          v: study.label
        },
        B4: {
          v: 'retrieve url u silly'
        },
        C4: {
          v: study.comment
        },
        D4: {
          v: GROUP_ALLOCATION_OPTIONS[studyInformation.allocation].label
        },
        E4: {
          v: BLINDING_OPTIONS[studyInformation.blinding].label
        },
        F4: {
          v: STATUS_OPTIONS[studyInformation.status].label
        },
        G4: {
          v: studyInformation.numberOfCenters
        },
        H4: {
          v: studyInformation.objective.comment
        }
      };
    }

    function buildArmData(arms) {
      return _.reduce(arms, function(acc, arm, idx) {
        var rowNum = (4 + idx);
        acc['K' + rowNum] = {
          v: arm.label
        };
        acc['L' + rowNum] = {
          v: arm.comment
        };
        return acc;
      }, {});
    }

    function buildArmMerges(arms) {
      return _.map(_.range(0, 10), function(i) {
        return {
          s: {
            c: i,
            r: 3
          },
          e: {
            c: i,
            r: 3 + arms.length - 1
          }
        };
      });
    }

    function buildPopulationInformationData(populationInformation) {
      return {
        I4: {
          v: populationInformation.indication.label
        },
        J4: {
          v: populationInformation.eligibilityCriteria.label
        }
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

    function buildActivitiesSheet(activities) {
      var doseTypes = {
        'ontology:FixedDoseDrugTreatment': 'fixed',
        'ontology:TitratedDoseDrugTreatment': 'titrated'
      };

      var sheet = {
        A1: {
          v: 'id'
        },
        B1: {
          v: 'title'
        },
        C1: {
          v: 'type'
        },
        D1: {
          v: 'description'
        }
      };

      var maxTreatments = _.max(_.map(activities, function(activity) {
        return activity.treatments ? activity.treatments.length : 0;
      }));
      var colHeaders = _.reduce(_.range(0, maxTreatments), function(accum, i) {
        accum[XLSX.utils.encode_col(4 + i * 6) + 1] = {
          v: 'drug label'
        };
        accum[XLSX.utils.encode_col(5 + i * 6) + 1] = {
          v: 'dose type'
        };
        accum[XLSX.utils.encode_col(6 + i * 6) + 1] = {
          v: 'dose'
        };
        accum[XLSX.utils.encode_col(7 + i * 6) + 1] = {
          v: 'max dose'
        };
        accum[XLSX.utils.encode_col(8 + i * 6) + 1] = {
          v: 'unit'
        };
        accum[XLSX.utils.encode_col(9 + i * 6) + 1] = {
          v: 'periodicity'
        };
        return accum;
      }, {});

      var activityData = _.reduce(activities, function(accum, activity, index) {
        var row = index + 2;
        accum[XLSX.utils.encode_col(0) + row] = {
          v: activity.activityUri
        };
        accum[XLSX.utils.encode_col(1) + row] = {
          v: activity.label
        };
        accum[XLSX.utils.encode_col(2) + row] = {
          v: activity.activityType.label
        };
        accum[XLSX.utils.encode_col(3) + row] = {
          v: activity.activityDescription
        };

        if (activity.activityType.uri === 'ontology:TreatmentActivity') {
          _.forEach(activity.treatments, function(treatment, index) {
            var isFixedDose = treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment';
            accum[XLSX.utils.encode_col(4 + index * 6) + row] = {
              v: treatment.drug.label
            };
            accum[XLSX.utils.encode_col(5 + index * 6) + row] = {
              v: doseTypes[treatment.treatmentDoseType]
            };
            accum[XLSX.utils.encode_col(6 + index * 6) + row] = {
              v: isFixedDose ? treatment.fixedValue : treatment.minValue
            };
            accum[XLSX.utils.encode_col(7 + index * 6) + row] = {
              v: isFixedDose ? undefined : treatment.maxValue
            };
            accum[XLSX.utils.encode_col(8 + index * 6) + row] = {
              v: treatment.doseUnit.label
            };
            accum[XLSX.utils.encode_col(9 + index * 6) + row] = {
              v: treatment.dosingPeriodicity
            };
          });
        }
        return accum;
      }, {});

      return _.merge({
        '!ref': 'A1:' + XLSX.utils.encode_col(4 + maxTreatments * 6) + '' + activities.length
      }, sheet, colHeaders, activityData);
    }

    // interface
    return {
      exportStudy: exportStudy
    };

  };
  return dependencies.concat(ExcelExportService);
});