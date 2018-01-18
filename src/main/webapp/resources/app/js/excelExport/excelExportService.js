'use strict';
define(['lodash', 'xlsx-shim', 'file-saver'], function(_, XLSX, saveAs) {
  var dependencies = ['$q', 'GROUP_ALLOCATION_OPTIONS', 'BLINDING_OPTIONS', 'STATUS_OPTIONS',
    'StudyService',
    'ArmService',
    'StudyInformationService',
    'PopulationInformationService',
    'EpochService',
    'ActivityService',
    'StudyDesignService'
  ];
  var ExcelExportService = function($q, GROUP_ALLOCATION_OPTIONS, BLINDING_OPTIONS, STATUS_OPTIONS,
    StudyService,
    ArmService,
    StudyInformationService,
    PopulationInformationService,
    EpochService,
    ActivityService,
    StudyDesignService) {

    function exportStudy() {
      var promises = [StudyService.getJsonGraph()];
      promises = promises.concat(_.map([
        ArmService,
        StudyInformationService,
        PopulationInformationService,
        EpochService,
        ActivityService,
        StudyDesignService
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

        var workBook = XLSX.utils.book_new();

        var studyDataSheet = buildStudyDataSheet();
        var studyData = buildStudyInformation(study, studyInformation);

        var armData = buildArmData(arms);
        var armMerges = buildArmMerges(arms);

        var treatmentLabels = buildTreatmentLabels(arms, epochs, activities, studyDesign);

        var populationInformationData = buildPopulationInformationData(populationInformation);

        _.merge(studyDataSheet, studyData, populationInformationData, armData, treatmentLabels);

        studyDataSheet['!merges'] = studyDataSheet['!merges'].concat(armMerges);

        var activitiesSheet = buildActivitiesSheet(activities);

        XLSX.utils.book_append_sheet(workBook, studyDataSheet, 'Study data');
        XLSX.utils.book_append_sheet(workBook, activitiesSheet, 'Activities');
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

    function buildStudyDataSheet() {
      var studyDataSheet = {
        '!ref': 'A1:N10', //fixme
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