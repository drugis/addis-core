'use strict';
define(['lodash', 'xlsx-shim', 'angular', 'angular-mocks'], function(_, XLSX) {
  describe('the excel export util service', function() {
    var rootScope, q;
    var excelExportUtilService;
    var resultsService = jasmine.createSpyObj('ResultsService', ['queryResults']);
    var GROUP_ALLOCATION_OPTIONS = {
      randomized: {
        label: 'randomized'
      }
    };
    var BLINDING_OPTIONS = {
      double: {
        label: 'double'
      }
    };
    var STATUS_OPTIONS = {
      completed: {
        label: 'completed'
      }
    };
    var promise1;
    var promise2;
    var promise3;
    var promise1defer;
    var promise2defer;
    var promise3defer;
    var startRows = {
      'Study data': 0,
      'Activities': 0,
      'Epochs': 0,
      'Study design': 0,
      'Measurement moments': 0,
      'Concepts': 0
    };
    var arms = [{
      armURI: 'arm1Uri',
      label: 'arm 1'
    }, {
      armURI: 'arm2Uri',
      label: 'arm 2'
    }];
    var epochs = [{
      uri: 'epoch1Uri',
      label: 'epoch1label',
      comment: 'epoch1comment',
      duration: 'P1W',
      isPrimary: false
    }, {
      uri: 'epoch2Uri',
      label: 'epoch2label',
      comment: 'epoch2comment',
      duration: 'P1D',
      isPrimary: true
    }];
    var studyDesign = [{
      epochUri: 'epoch1Uri',
      armUri: 'arm1Uri',
      activityUri: 'randomizationUri'
    }, {
      epochUri: 'epoch1Uri',
      armUri: 'arm2Uri',
      activityUri: 'randomizationUri'
    }, {
      epochUri: 'epoch2Uri',
      armUri: 'arm1Uri',
      activityUri: 'fixedSingleDoseUri'
    }, {
      epochUri: 'epoch2Uri',
      armUri: 'arm2Uri',
      activityUri: 'combiTreatmentUri'
    }];

    var activities = [{
      activityUri: 'randomizationUri',
      label: 'Randomization',
      activityType: {
        label: 'Randomization',
        type: 'ontology:Randomization'
      }
    }, {
      activityUri: 'fixedSingleDoseUri',
      label: 'Fixed',
      activityType: {
        label: 'fixed',
        uri: 'ontology:TreatmentActivity'
      },
      treatments: [{
        treatmentDoseType: 'ontology:FixedDoseDrugTreatment',
        fixedValue: 1,
        drug: {
          uri: 'drug1Uri',
          label: 'drug1'
        },
        doseUnit: {
          label: 'mg',
          uri: 'milligramUri'
        },
        dosingPeriodicity: 'P1D'
      }]
    }, {
      activityUri: 'combiTreatmentUri',
      label: 'Combination',
      activityType: {
        label: 'combination',
        uri: 'ontology:TreatmentActivity'
      },
      treatments: [{
        treatmentDoseType: 'ontology:FixedDoseDrugTreatment',
        fixedValue: 2,
        drug: {
          uri: 'drug1Uri',
          label: 'drug1'
        },
        doseUnit: {
          label: 'mg',
          uri: 'milligramUri'
        },
        dosingPeriodicity: 'P1D'
      }, {
        treatmentDoseType: 'ontology:TitratedDoseDrugTreatment',
        minValue: 3,
        maxValue: 4,
        drug: {
          uri: 'drug2Uri',
          label: 'drug2'
        },
        doseUnit: {
          label: 'mg',
          uri: 'milligramUri'
        },
        dosingPeriodicity: 'P1D'
      }]
    }];
    var conceptsSheet = {
      A2: cellValue('variable1Uri'),
      B2: cellValue('variable 1'),
      A3: cellValue('variable2Uri'),
      B3: cellValue('variable 2'),
      A4: cellValue('variable3Uri'),
      B4: cellValue('variable 3'),
      A5: cellValue('variable4Uri'),
      B5: cellValue('variable 4'),
      A6: cellValue('drug1Uri'),
      B6: cellValue('drug1'),
      A7: cellValue('drug2Uri'),
      B7: cellValue('drug2'),
      A8: cellValue('milligramUri'),
      B8: cellValue('milligram')
    };

    beforeEach(function() {
      module('addis.excelIO', function($provide) {
        $provide.value('$location', {
          absUrl: function() {
            return 'studyUrl';
          }
        });
        $provide.value('GROUP_ALLOCATION_OPTIONS', GROUP_ALLOCATION_OPTIONS);
        $provide.value('BLINDING_OPTIONS', BLINDING_OPTIONS);
        $provide.value('STATUS_OPTIONS', STATUS_OPTIONS);
        $provide.value('ResultsService', resultsService);
      });
    });

    beforeEach(inject(function($q, $rootScope, ExcelExportUtilService) {
      q = $q;
      rootScope = $rootScope;
      excelExportUtilService = ExcelExportUtilService;
      promise1defer = q.defer();
      promise2defer = q.defer();
      promise3defer = q.defer();
      promise1 = promise1defer.promise;
      promise2 = promise2defer.promise;
      promise3 = promise3defer.promise;
      promise1defer.resolve(1);
      promise2defer.resolve(2);
      promise3defer.resolve(3);
      resultsService.queryResults.and.returnValues(promise1, promise2, promise3);
    }));

    describe('getVariableResults', function() {
      it('should add concept types to the variables and return the result and other promises', function(done) {
        var otherPromises = [{
          uri: 'someUri'
        }];
        var populationCharacteristics = [{
          uri: 'popCharUri1'
        }];
        var outcomes = [{
          uri: 'outcomeUri1'
        }];
        var adverseEvents = [{
          uri: 'adverseUri1'
        }];
        var variableResults = [populationCharacteristics, outcomes, adverseEvents];
        excelExportUtilService.getVariableResults(otherPromises, variableResults).then(function(result) {

          var populationCharacteristicsExpectation = [{
            uri: 'popCharUri1',
            type: 'baseline characteristic'
          }];
          var outcomesExpectation = [{
            uri: 'outcomeUri1',
            type: 'outcome'
          }];
          var adverseEventsExpectation = [{
            uri: 'adverseUri1',
            type: 'adverse event'
          }];
          var expectedResult = otherPromises.concat(
            [populationCharacteristicsExpectation, outcomesExpectation, adverseEventsExpectation], [{
              uri: 'popCharUri1',
              results: 1
            }, {
              uri: 'outcomeUri1',
              results: 2
            }, {
              uri: 'adverseUri1',
              results: 3
            }]);
          expect(result).toEqual(expectedResult);
          done();
        });
        rootScope.$apply();
      });
    });

    describe('buildConceptsSheet', function() {
      var studyConcepts = [{
        uri: 'drugUri',
        label: 'drug',
        type: 'drug',
      }, {
        uri: 'variableUri',
        label: 'variable',
        type: 'variable',
        conceptMapping: 'datasetVariableUri'
      }, {
        uri: 'unitUri',
        label: 'mg',
        type: 'unit',
        conceptMapping: 'datasetGramUri',
        conversionMultiplier: 0.001
      }];

      var expectedResultNoOffset = {
        '!ref': 'A1:E4',
        A1: cellValue('id'),
        B1: cellValue('label'),
        C1: cellValue('type'),
        D1: cellValue('dataset concept uri'),
        E1: cellValue('multiplier'),
        A2: cellValue('drugUri'),
        B2: cellValue('drug'),
        C2: cellValue('drug'),
        E2: cellNumber(undefined),
        A3: cellValue('variableUri'),
        B3: cellValue('variable'),
        C3: cellValue('variable'),
        D3: cellValue('datasetVariableUri'),
        E3: cellNumber(undefined),
        A4: cellValue('unitUri'),
        B4: cellValue('mg'),
        C4: cellValue('unit'),
        D4: cellValue('datasetGramUri'),
        E4: cellNumber(0.001),
      };
      var offset = 4;
      var expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
      expectedResultWithOffset['!ref'] = 'A1:E8';

      it('should generate the conceps sheet', function() {
        var result = excelExportUtilService.buildConceptsSheet(startRows, studyConcepts);
        expect(result).toEqual(expectedResultNoOffset);
      });
      it('should generate the concepts sheet with offset', function() {
        var startRows = {
          Concepts: offset
        };
        var result = excelExportUtilService.buildConceptsSheet(startRows, studyConcepts);
        expect(result).toEqual(expectedResultWithOffset);
      });
    });

    describe('buildMeasurementMomentSheet', function() {
      var measurementMoments = [{
        uri: 'measurementMoment1Uri',
        label: 'name',
        epochUri: 'epochRef',
        relativeToAnchor: 'ontology:anchorEpochStart',
        offset: 'offset'
      }];
      var epochSheet = {
        B2: cellValue('epochRef'),
        C2: cellValue('something')
      };
      var expectedResultNoOffset = {
        '!ref': 'A1:E2',
        A1: cellValue('id'),
        B1: cellValue('name'),
        C1: cellValue('epoch'),
        D1: cellValue('from'),
        E1: cellValue('offset'),
        A2: cellValue('measurementMoment1Uri'),
        B2: cellValue('name'),
        C2: {
          v: epochSheet.C2.v,
          f: 'Epochs!C2'
        },
        D2: cellValue('start'),
        E2: cellValue('offset')
      };
      var offset = 7;
      var expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
      expectedResultWithOffset['!ref'] = 'A1:E9';
      it('should generate the measurement moment sheet', function() {
        var result = excelExportUtilService.buildMeasurementMomentSheet(startRows, measurementMoments, epochSheet);
        expect(result).toEqual(expectedResultNoOffset);
      });
      it('should generate the measurement moment sheet with an offset', function() {
        var startRows = {
          'Measurement moments': offset
        };
        var result = excelExportUtilService.buildMeasurementMomentSheet(startRows, measurementMoments, epochSheet);
        expect(result).toEqual(expectedResultWithOffset);
      });
    });

    describe('buildStudyDesignSheet', function() {

      var epochSheet = {
        A2: cellValue('epoch1Uri'),
        A3: cellValue('epoch2Uri'),
        B2: cellValue('randomisation'),
        B3: cellValue('treatment phase')
      };
      var activitiesSheet = {
        A2: cellValue('randomizationUri'),
        A3: cellValue('fixedSingleDoseUri'),
        A4: cellValue('combiTreatmentUri'),
        B2: cellValue('randomisation'),
        B3: cellValue('fixed treatment'),
        B4: cellValue('combi TreatmentActivity')
      };
      var studyDataSheet = {
        K4: cellValue('arm 1'),
        K5: cellValue('arm 2')
      };

      var expectedResultNoOffset = {
        '!ref': 'A1:D3',
        A1: cellValue('arm'),
        B1: {
          v: epochSheet.B2.v,
          f: 'Epochs!B2'
        },
        C1: {
          v: epochSheet.B3.v,
          f: 'Epochs!B3'
        },
        A2: {
          v: studyDataSheet.K4.v,
          f: '\'Study data\'!K4'
        },
        B2: {
          v: activitiesSheet.B2.v,
          f: 'Activities!B2'
        },
        C2: {
          v: activitiesSheet.B3.v,
          f: 'Activities!B3'
        },
        A3: {
          v: studyDataSheet.K5.v,
          f: '\'Study data\'!K5'
        },
        B3: {
          v: activitiesSheet.B2.v,
          f: 'Activities!B2'
        },
        C3: {
          v: activitiesSheet.B4.v,
          f: 'Activities!B4'
        }
      };
      var offset = 9;
      var expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset, true);
      expectedResultWithOffset['!ref'] = 'A1:D12';

      it('should build the study design table sheet', function() {
        var result = excelExportUtilService.buildStudyDesignSheet(startRows, epochs, arms, studyDesign, epochSheet, activitiesSheet,
          studyDataSheet);
        expect(result).toEqual(expectedResultNoOffset);
      });

      it('should build the study design table sheet with offset', function() {
        var startRows = {
          'Study design': offset,
          'Study data': 0
        };
        var result = excelExportUtilService.buildStudyDesignSheet(startRows, epochs, arms, studyDesign, epochSheet, activitiesSheet,
          studyDataSheet);
        expect(result).toEqual(expectedResultWithOffset);
      });
    });

    describe('buildEpochSheet', function() {
      var expectedResultNoOffset = {
        '!ref': 'A1:E3',
        A1: cellValue('id'),
        B1: cellValue('name'),
        C1: cellValue('description'),
        D1: cellValue('duration'),
        E1: cellValue('Is primary?'),
        A2: cellValue('epoch1Uri'),
        B2: cellValue('epoch1label'),
        C2: cellValue('epoch1comment'),
        D2: cellValue('P1W'),
        E2: cellValue(false),
        A3: cellValue('epoch2Uri'),
        B3: cellValue('epoch2label'),
        C3: cellValue('epoch2comment'),
        D3: cellValue('P1D'),
        E3: cellValue(true)
      };
      var offset = 11;
      var expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
      expectedResultWithOffset['!ref'] = 'A1:E14';
      it('should generate the epoch worksheet', function() {
        var result = excelExportUtilService.buildEpochSheet(startRows, epochs);
        expect(result).toEqual(expectedResultNoOffset);
      });
      it('should generate the epoch worksheet with an offset', function() {
        var startRows = {
          Epochs: offset
        };
        var result = excelExportUtilService.buildEpochSheet(startRows, epochs);
        expect(result).toEqual(expectedResultWithOffset);
      });
    });

    describe('buildActivitiesSheet', function() {
      var expectedResultNoOffset = {
        '!ref': 'A1:Q4',
        A1: cellValue('id'),
        B1: cellValue('title'),
        C1: cellValue('type'),
        D1: cellValue('description'),
        E1: cellValue('drug label'),
        F1: cellValue('dose type'),
        G1: cellValue('dose'),
        H1: cellValue('max dose'),
        I1: cellValue('unit'),
        J1: cellValue('periodicity'),
        K1: cellValue('drug label'),
        L1: cellValue('dose type'),
        M1: cellValue('dose'),
        N1: cellValue('max dose'),
        O1: cellValue('unit'),
        P1: cellValue('periodicity'),
        A2: cellValue('randomizationUri'),
        B2: cellValue('Randomization'),
        C2: cellValue('Randomization'),
        D2: cellValue(undefined),
        A3: cellValue('fixedSingleDoseUri'),
        B3: cellValue('Fixed'),
        C3: cellValue('fixed'),
        D3: cellValue(undefined),
        E3: {
          v: 'drug1',
          f: 'Concepts!B6'
        },
        F3: cellValue('fixed'),
        G3: cellNumber(1),
        H3: cellNumber(undefined),
        I3: {
          v: 'milligram',
          f: 'Concepts!B8'
        },
        J3: cellValue('P1D'), //
        A4: cellValue('combiTreatmentUri'),
        B4: cellValue('Combination'),
        C4: cellValue('combination'),
        D4: cellValue(undefined),
        E4: {
          v: 'drug1',
          f: 'Concepts!B6'
        },
        F4: cellValue('fixed'),
        G4: cellNumber(2),
        H4: cellNumber(undefined),
        I4: {
          v: 'milligram',
          f: 'Concepts!B8'
        },
        J4: cellValue('P1D'), //
        K4: {
          v: 'drug2',
          f: 'Concepts!B7'
        },
        L4: cellValue('titrated'),
        M4: cellNumber(3),
        N4: cellNumber(4),
        O4: {
          v: 'milligram',
          f: 'Concepts!B8'
        },
        P4: cellValue('P1D')
      };
      var offset = 6;
      var expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
      expectedResultWithOffset['!ref'] = 'A1:Q10';
      it('should generate the activities sheet', function() {
        var result = excelExportUtilService.buildActivitiesSheet(startRows, activities, conceptsSheet);
        expect(result).toEqual(expectedResultNoOffset);
      });
      it('should generate the activities sheet', function() {
        var startRows = {
          Activities: offset
        };
        var result = excelExportUtilService.buildActivitiesSheet(startRows, activities, conceptsSheet);
        expect(result).toEqual(expectedResultWithOffset);
      });
    });

    describe('buildStudyDataSheet', function() {
      var study = {
        label: 'Study 1',
        comment: 'A long study title 1',
        has_included_population: [{
          '@id': 'overallPopulationUri'
        }]
      };
      var studyInformation = {
        allocation: 'randomized',
        blinding: 'double',
        status: 'completed',
        numberOfCenters: 3,
        objective: {
          comment: 'comment'
        }
      };
      var studyUrl = 'http://some.study.url/';
      var populationInformation = {
        indication: {
          label: 'severe depression'
        },
        eligibilityCriteria: {
          label: 'inclusion: everyone; exclusion: everyone else'
        }
      };
      var measurementMomentSheet = {
        A1: cellValue('measurementMoment1Uri'),
        A2: cellValue('measurement moment 1'),
        B1: cellValue('measurementMoment2Uri'),
        B2: cellValue('measurement moment 2'),
        C1: cellValue('measurementMoment3Uri')
      };
      var variables = [{
        uri: 'variable1Uri',
        conceptMapping: 'variable1Uri',
        label: 'Age (years)',
        measuredAtMoments: [{
          uri: 'measurementMoment1Uri',
        }, {
          uri: 'measurementMoment2Uri'
        }],
        measurementType: 'ontology:dichotomous',
        resultProperties: [
          'http://trials.drugis.org/ontology#sample_size',
          'http://trials.drugis.org/ontology#count'
        ],
        results: [{
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'sample_size',
          value: 123
        }, {
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'count',
          value: 37
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'sample_size',
          value: 321
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'count',
          value: 42
        }, {
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment2Uri',
          result_property: 'sample_size',
          value: 234
        }, {
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment2Uri',
          result_property: 'count',
          value: 73
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment2Uri',
          result_property: 'sample_size',
          value: 432
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment2Uri',
          result_property: 'count',
          value: 24
        }, {
          armUri: 'overallPopulationUri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'count',
          value: 500
        }],
        type: 'baseline characteristic'
      }, {
        uri: 'variable2Uri',
        conceptMapping: 'variable2Uri',
        label: 'Sex',
        measuredAtMoments: [{
          uri: 'measurementMoment1Uri',
        }],
        measurementType: 'ontology:categorical',
        categoryList: [{
          '@id': 'categoryMaleUri',
          label: 'male'
        }, {
          '@id': 'categoryFemaleUri',
          label: 'female'
        }],
        results: [{
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: {
            category: 'categoryMaleUri'
          },
          value: 119
        }, {
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: {
            category: 'categoryFemaleUri'
          },
          value: 201
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: {
            category: 'categoryMaleUri'
          },
          value: 301
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: {
            category: 'categoryFemaleUri'
          },
          value: 401
        }],
        type: 'baseline characteristic'
      }, {
        uri: 'variable3Uri',
        label: 'Unmeasured',
        measuredAtMoments: [],
        measurementType: 'ontology:continuous',
        resultProperties: [
          'http://trials.drugis.org/ontology#sample_size',
          'http://trials.drugis.org/ontology#mean',
          'http://trials.drugis.org/ontology#standard_deviation'
        ],
        type: 'adverse event'
      }, {
        uri: 'variable4Uri',
        label: 'survival variable',
        measuredAtMoments: [{
          uri: 'measurementMoment1Uri'
        }],
        measurementType: 'ontology:survival',
        resultProperties: [
          'http://trials.drugis.org/ontology#count',
          'http://trials.drugis.org/ontology#exposure'
        ],
        results: [{
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'count',
          value: 123
        }, {
          armUri: 'arm1Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'exposure',
          value: 3789
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'count',
          value: 321
        }, {
          armUri: 'arm2Uri',
          momentUri: 'measurementMoment1Uri',
          result_property: 'exposure',
          value: 45678
        }],
        timeScale: 'P1Y',
        type: 'adverse event'
      }];
      var expectedResultNoOffset = {
        '!merges': [],
        '!ref': 'A1:AJ6',
        //headers
        //row 1 (data categories)
        A1: cellValue('Study Information'),
        I1: cellValue('Population Information'),
        K1: cellValue('Arm Information'),
        M1: cellValue('Measurement Information'),
        //row 2 (variable names)
        M2: cellFormula('Concepts!B2', conceptsSheet.B2.v),
        U2: cellFormula('Concepts!B3', conceptsSheet.B3.v),
        Z2: cellFormula('Concepts!B4', conceptsSheet.B4.v),
        AE2: cellFormula('Concepts!B5', conceptsSheet.B5.v),
        // row 3 )variable detail headers
        A3: cellValue('id'), //row 3
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
        M3: cellValue('variable type'),
        N3: cellValue('measurement type'),
        O3: cellValue('measurement moment'),
        P3: cellValue('sample_size'),
        Q3: cellValue('count'),
        R3: cellValue('measurement moment'),
        S3: cellValue('sample_size'),
        T3: cellValue('count'),
        U3: cellValue('variable type'),
        V3: cellValue('measurement type'),
        W3: cellValue('measurement moment'),
        X3: cellValue('male'),
        Y3: cellValue('female'),
        Z3: cellValue('variable type'),
        AA3: cellValue('measurement type'),
        AB3: cellValue('sample_size'),
        AC3: cellValue('mean'),
        AD3: cellValue('standard_deviation'),
        AE3: cellValue('variable type'),
        AF3: cellValue('measurement type'),
        AG3: cellValue('time scale'),
        AH3: cellValue('measurement moment'),
        AI3: cellValue('count'),
        AJ3: cellValue('exposure'),
        // data rows
        // arm 1
        A4: cellValue(study.label),
        B4: _.merge(cellValue(studyUrl), {
          l: {
            Target: studyUrl
          }
        }),
        C4: cellValue(study.comment),
        D4: cellValue(GROUP_ALLOCATION_OPTIONS[studyInformation.allocation].label),
        E4: cellValue(BLINDING_OPTIONS[studyInformation.blinding].label),
        F4: cellValue(STATUS_OPTIONS[studyInformation.status].label),
        G4: cellNumber(studyInformation.numberOfCenters),
        H4: cellValue(studyInformation.objective.comment),
        I4: cellValue(populationInformation.indication.label),
        J4: cellValue(populationInformation.eligibilityCriteria.label),
        K4: cellValue(arms[0].label),
        L4: cellValue(arms[0].comment),
        M4: cellValue('baseline characteristic'),
        N4: cellValue('dichotomous'),
        O4: cellFormula('\'Measurement moments\'!B1', measurementMomentSheet.B1.v),
        P4: cellNumber(123),
        Q4: cellNumber(37),
        R4: cellFormula('\'Measurement moments\'!C1', measurementMomentSheet.C1.v),
        S4: cellNumber(234),
        T4: cellNumber(73),
        U4: cellValue('baseline characteristic'),
        V4: cellValue('categorical'),
        W4: cellFormula('\'Measurement moments\'!B1', measurementMomentSheet.B1.v),
        X4: cellNumber(119),
        Y4: cellNumber(201),
        Z4: cellValue('adverse event'),
        AA4: cellValue('continuous'),
        AE4: cellValue('adverse event'),
        AF4: cellValue('survival'),
        AG4: cellValue('P1Y'),
        AH4: cellFormula('\'Measurement moments\'!B1', measurementMomentSheet.B1.v),
        AI4: cellNumber(123),
        AJ4: cellNumber(3789),


        // arm 2
        K5: cellValue(arms[1].label),
        L5: cellValue(arms[1].comment),
        P5: cellNumber(321),
        Q5: cellNumber(42),
        S5: cellNumber(432),
        T5: cellNumber(24),
        X5: cellNumber(301),
        Y5: cellNumber(401),
        AI5: cellNumber(321),
        AJ5: cellNumber(45678),

        // overall population
        K6: cellValue('Overall population'),
        L6: cellValue(undefined),
        Q6: cellNumber(500)
      };
      expectedResultNoOffset['!merges'] = [
        cellRange(0, 0, 7, 0),
        cellRange(8, 0, 9, 0),
        cellRange(10, 0, 11, 0),
        cellRange(12, 0, 35, 0),
        cellRange(12, 3, 12, 5),
        cellRange(13, 3, 13, 5),
        cellRange(14, 3, 14, 5),
        cellRange(17, 3, 17, 5),
        cellRange(12, 1, 19, 1),
        cellRange(20, 3, 20, 5),
        cellRange(21, 3, 21, 5),
        cellRange(22, 3, 22, 5),
        cellRange(20, 1, 24, 1),
        cellRange(25, 3, 25, 5),
        cellRange(26, 3, 26, 5),
        cellRange(25, 1, 26, 1),
        cellRange(27, 3, 27, 5),
        cellRange(28, 3, 28, 5),
        cellRange(29, 3, 29, 5),
        cellRange(27, 1, 32, 1),
      ];

      expectedResultNoOffset['!merges'] = expectedResultNoOffset['!merges'].concat(_.map(_.range(0, 10), function(i) {
        return cellRange(i, 3, i, 3 + arms.length);
      }));

      var offset = 5;
      var expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);

      for (var i = 4; i < expectedResultWithOffset['!merges'].length; ++i) {
        expectedResultWithOffset['!merges'][i].s.r += offset;
        expectedResultWithOffset['!merges'][i].e.r += offset;
      }
      expectedResultWithOffset['!ref'] = 'A1:AJ11';

      it('should generate the study data worksheet', function() {
        var result = excelExportUtilService.buildStudyDataSheet(startRows, study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
          populationInformation, variables, conceptsSheet, measurementMomentSheet);
        expect(result).toEqual(expectedResultNoOffset);
      });
      it('should generate the study data worksheet correctly if there is an offset', function() {
        var offsetStartRows = {
          'Study data': offset
        };
        var result = excelExportUtilService.buildStudyDataSheet(offsetStartRows, study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
          populationInformation, variables, conceptsSheet, measurementMomentSheet);
        expect(result).toEqual(expectedResultWithOffset);
      });
    });

    describe('arrayToA1FromCoordinate', function() {
      it('should create an A1-indexed data object from a 2D array, starting from the anchor coordinate', function() {
        var data = [
          [1, 2, 3],
          [4, 5, 6]
        ];
        var expectedResult = {
          D5: 1,
          D6: 2,
          D7: 3,
          E5: 4,
          E6: 5,
          E7: 6
        };
        var result = excelExportUtilService.arrayToA1FromCoordinate(3, 4, data); // anchor D5
        expect(result).toEqual(expectedResult);
      });
    });

    describe('mergePreservingRange', function() {
      it('should set the ref so it contains the ranges of both sheets if one fits within the other', function() {
        var source = {
          a: 'b',
          '!ref': 'A1:B2'
        };
        var target = {
          c: 'd',
          '!ref': 'A1:D5'
        };

        var expectedResult = {
          a: 'b',
          c: 'd',
          '!ref': 'A1:D5'
        };

        var result = excelExportUtilService.mergePreservingRange(source, target);

        expect(result).toEqual(expectedResult);
      });
      it('should set the ref so it contains the ranges of both sheets if both ranges stretch each other', function() {
        var source = {
          a: 'b',
          '!ref': 'A1:D2'
        };
        var target = {
          c: 'd',
          '!ref': 'A1:B5'
        };

        var expectedResult = {
          a: 'b',
          c: 'd',
          '!ref': 'A1:D5'
        };

        var result = excelExportUtilService.mergePreservingRange(target, source);

        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildDatasetInformationSheet', function() {
      it('should build a dataset information sheet', function() {
        var datasetWithCoordinates = {
          title: 'testDataset',
          comment: 'dataset for testing',
          url: 'http://localhost/datasets/1'
        };

        var expectedResult = {
          A1: cellValue('title'),
          B1: cellValue('ADDIS url'),
          C1: cellValue('description'),
          A2: cellValue(datasetWithCoordinates.title),
          B2: cellLink(datasetWithCoordinates.url),
          C2: cellValue(datasetWithCoordinates.comment),
          '!ref': 'A1:C2'
        };

        var result = excelExportUtilService.buildDatasetInformationSheet(datasetWithCoordinates);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildDatasetConceptsSheet', function() {
      it('should generate the dataset concepts sheet', function() {
        var concepts = [{
          uri: 'http://unitConceptUri',
          label: 'milligram',
          type: {
            label: 'Unit'
          }
        }, {
          uri: 'http://drugConceptUri',
          label: 'drug 1',
          type: {
            label: 'Drug'
          }
        }];

        var expectedResult = {
          A1: cellValue('id'),
          B1: cellValue('label'),
          C1: cellValue('type'),
          A2: cellValue('http://unitConceptUri'),
          B2: cellValue('milligram'),
          C2: cellValue('Unit'),
          A3: cellValue('http://drugConceptUri'),
          B3: cellValue('drug 1'),
          C3: cellValue('Drug'),
          '!ref': 'A1:C3'
        };
        var result = excelExportUtilService.buildDatasetConceptsSheet(concepts);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('addStudyHeaders', function() {
      it('should add study headers to the sheets', function() {

        var startRows = {
          'Study data': 1,
          'something': 33,
          'Study design': 555
        };
        var workbook = {
          Sheets: {
            'Study data': {
              foo: 'bar',
              A5: {
                v: 'a5value'
              }
            },
            'something': {
              goo: 'car'
            },
            'Study design': {
              hoo: 'dar'
            }
          }
        };

        var expectedResult = {
          Sheets: {
            'Study data': {
              foo: 'bar',
              A5: {
                v: 'a5value'
              }
            },
            'something': {
              goo: 'car',
              A34: cellFormula('\'Study data\'!A5', workbook.Sheets['Study data'].A5.v)
            },
            'Study design': {
              hoo: 'dar',
              A555: cellFormula('\'Study data\'!A5', workbook.Sheets['Study data'].A5.v)
            }
          }
        };

        var result = excelExportUtilService.addStudyHeaders(workbook, startRows);
        expect(result).toEqual(expectedResult);

      });
    });

    describe('getStudyUrl', function() {
      it('should return the url of the study', function() {
        var root = 'http://root.com/';
        var coordinates = {
          userUid: 1,
          datasetUuid: 'datasetUuid',
          versionUuid: 'versionUuid',
          graphUuid: 'graphUuid'
        };
        var result = excelExportUtilService.getStudyUrl(root, coordinates);
        var expectedResult = 'http://root.com//users/1/datasets/datasetUuid/versions/versionUuid/studies/graphUuid';
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildStartRows', function() {
      it('should return the starting rows considering a offset', function() {
        var offset = 2;
        var result = excelExportUtilService.buildStartRows(offset);
        var expectedResult = {
          'Study data': 0,
          Activities: 2,
          Epochs: 2,
          'Study design': 2,
          'Measurement moments': 2,
          Concepts: 2
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('updateStartRows', function() {
      it('should update the start rows to the last position of each sheet', function() {
        var workbook = {
          Sheets: {
            'Study data': {
              '!ref': 'A1:D7'
            },
            Activities: {
              '!ref': 'A1:D4'
            },
            Epochs: {
              '!ref': 'A1:D86'
            },
            'Study design': {
              '!ref': 'A1:D31'
            },
            'Measurement moments': {
              '!ref': 'A1:D1'
            },
            Concepts: {
              '!ref': 'A1:D6'
            }
          }
        };
        var result = excelExportUtilService.updateStartRows(workbook);

        var expectedResult = {
          'Study data': 7,
          Activities: 5,
          Epochs: 87,
          'Study design': 33,
          'Measurement moments': 2,
          Concepts: 7
        };
        expect(result).toEqual(expectedResult);
      });
    });

    function shiftExpectations(expectedResultNoOffset, offset, moveFirstRow) {
      return _(_.cloneDeep(expectedResultNoOffset))
        .toPairs()
        .map(function(pair) {
          if (pair[0][0] === '!') {
            return pair;
          } else {
            var coords = XLSX.utils.decode_cell(pair[0]);
            coords.r += (!moveFirstRow && coords.r === 0) ? 0 : offset; // ignore main header row
            return [XLSX.utils.encode_cell(coords), pair[1]];
          }
        })
        .fromPairs()
        .value();
    }

    function cellNumber(value) {
      return {
        v: value,
        t: 'n'
      };
    }

    function cellFormula(formula, value) {
      return {
        f: formula,
        v: value
      };
    }

    function cellValue(value) {
      return {
        v: value
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

    function cellLink(url) {
      return {
        v: url,
        l: {
          Target: url
        }
      };
    }
  });
});