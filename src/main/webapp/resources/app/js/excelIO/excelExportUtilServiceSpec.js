'use strict';
define(['lodash', 'xlsx', 'angular-mocks', './excelIO'], function(_, XLSX) {
  describe('the excel export util service', function() {

    // workaround for making {foo: undefined} equal {}, see https://github.com/jasmine/jasmine/issues/592#issuecomment-43394093
    beforeEach((function () {
      var customMatchers = {
        toEqualObjectWithoutKey: function (utils, customEqualityTesters) {
          return {
            compare: function (actual, expected) {
              actual = removeUndefined(actual);
              expected = removeUndefined(expected);
              var result = {};
              result.pass = utils.equals(actual, expected, customEqualityTesters);
              if (!result.pass) {
                result.message = "Expected " + JSON.stringify(actual) + " toEqualObjectWithoutKey " + JSON.stringify(expected);
              }
              return result;
            }
          }
        }
      };
      jasmine.addMatchers(customMatchers);
    }));

    /**
     * remove key when value is undefined
     * @param {Object} obj
     * @returns {Object}
     */
    function removeUndefined(obj){
      var result = angular.copy(obj);
      traverse(result, function(key, value, isLeaf, parent, traversePath){
        if(value == undefined){
          delete parent[key];
        }
      });
      return result;
    }

    /**
     * traverse object
     * @param {Object} obj
     * @param {Function} callback as function(key, value, isLeaf, parent, traversePath)
     * @param {Array} [traversePath]
     */
    function traverse(obj, callback, traversePath) {
      for(var key in obj){
        if(obj.hasOwnProperty(key)){
          var item = obj[key];
          var path = traversePath || [];
          if (item instanceof Object && !(item instanceof Array)) {
            callback.apply(this, [key, item, false, obj, path]);
            var nextPath = angular.copy(path);
            nextPath.push(key);
            traverse(item, callback, nextPath);
          }else{
            callback.apply(this, [key, item, true, obj, path]);
          }
        }
      }
    }

    var rootScope, q;
    var excelExportUtilService;
    var IOU;
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
    var conceptsSheet;

    beforeEach(function() {
      angular.mock.module('addis.excelIO', function($provide) {
        $provide.value('$location', {
          absUrl: function() {
            return 'studyUrl';
          }
        });
        $provide.constant('GROUP_ALLOCATION_OPTIONS', GROUP_ALLOCATION_OPTIONS);
        $provide.constant('BLINDING_OPTIONS', BLINDING_OPTIONS);
        $provide.constant('STATUS_OPTIONS', STATUS_OPTIONS);
        $provide.value('ResultsService', resultsService);
      });
    });

    beforeEach(inject(function($q, $rootScope, ExcelExportUtilService, ExcelIOUtilService) {
      q = $q;
      rootScope = $rootScope;
      excelExportUtilService = ExcelExportUtilService;
      IOU = ExcelIOUtilService;
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
      conceptsSheet = {
        A2: IOU.cellValue('variable1Uri'),
        B2: IOU.cellValue('variable 1'),
        A3: IOU.cellValue('variable2Uri'),
        B3: IOU.cellValue('variable 2'),
        A4: IOU.cellValue('variable3Uri'),
        B4: IOU.cellValue('variable 3'),
        A5: IOU.cellValue('variable4Uri'),
        B5: IOU.cellValue('variable 4'),
        A6: IOU.cellValue('drug1Uri'),
        B6: IOU.cellValue('drug1'),
        A7: IOU.cellValue('drug2Uri'),
        B7: IOU.cellValue('drug2'),
        A8: IOU.cellValue('milligramUri'),
        B8: IOU.cellValue('milligram')
      };
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
      var offset = 4;

      var expectedResultNoOffset;
      var expectedResultWithOffset;
      beforeEach(function() {
        expectedResultNoOffset = {
          '!ref': 'A1:E4',
          A1: IOU.cellValue('id'),
          B1: IOU.cellValue('label'),
          C1: IOU.cellValue('type'),
          D1: IOU.cellValue('dataset concept uri'),
          E1: IOU.cellValue('multiplier'),
          A2: IOU.cellValue('drugUri'),
          B2: IOU.cellValue('drug'),
          C2: IOU.cellValue('drug'),
          D2: undefined,
          E2: IOU.cellNumber(undefined),
          A3: IOU.cellValue('variableUri'),
          B3: IOU.cellValue('variable'),
          C3: IOU.cellValue('variable'),
          D3: IOU.cellValue('datasetVariableUri'),
          E3: IOU.cellNumber(undefined),
          A4: IOU.cellValue('unitUri'),
          B4: IOU.cellValue('mg'),
          C4: IOU.cellValue('unit'),
          D4: IOU.cellValue('datasetGramUri'),
          E4: IOU.cellNumber(0.001),
        };
        expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
        expectedResultWithOffset['!ref'] = 'A1:E8';
      });

      it('should generate the concepts sheet', function() {
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
      var epochSheet;
      var expectedResultNoOffset;
      var offset = 7;
      var expectedResultWithOffset;

      beforeEach(function() {
        epochSheet = {
          B2: IOU.cellValue('epochRef'),
          C2: IOU.cellValue('something')
        };
        expectedResultNoOffset = {
          '!ref': 'A1:E2',
          A1: IOU.cellValue('id'),
          B1: IOU.cellValue('name'),
          C1: IOU.cellValue('epoch'),
          D1: IOU.cellValue('from'),
          E1: IOU.cellValue('offset'),
          A2: IOU.cellValue('measurementMoment1Uri'),
          B2: IOU.cellValue('name'),
          C2: {
            f: 'Epochs!C2'
          },
          D2: IOU.cellValue('start'),
          E2: IOU.cellValue('offset')
        };
        expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
        expectedResultWithOffset['!ref'] = 'A1:E9';
      });

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

      var epochSheet;
      var activitiesSheet;
      var studyDataSheet;

      var expectedResultNoOffset;
      var offset = 9;
      var expectedResultWithOffset;

      beforeEach(function() {
        epochSheet = {
          A2: IOU.cellValue('epoch1Uri'),
          A3: IOU.cellValue('epoch2Uri'),
          B2: IOU.cellValue('randomisation'),
          B3: IOU.cellValue('treatment phase')
        };
        activitiesSheet = {
          A2: IOU.cellValue('randomizationUri'),
          A3: IOU.cellValue('fixedSingleDoseUri'),
          A4: IOU.cellValue('combiTreatmentUri'),
          B2: IOU.cellValue('randomisation'),
          B3: IOU.cellValue('fixed treatment'),
          B4: IOU.cellValue('combi TreatmentActivity')
        };
        studyDataSheet = {
          K4: IOU.cellValue('arm 1'),
          K5: IOU.cellValue('arm 2')
        };

        expectedResultNoOffset = {
          '!ref': 'A1:D3',
          A1: IOU.cellValue('arm'),
          B1: IOU.cellFormula('Epochs!B2'),
          C1: IOU.cellFormula('Epochs!B3'),
          A2: IOU.cellFormula('\'Study data\'!K4'),
          B2: IOU.cellFormula('Activities!B2'),
          C2: IOU.cellFormula('Activities!B3'),
          A3: IOU.cellFormula('\'Study data\'!K5'),
          B3: IOU.cellFormula('Activities!B2'),
          C3: IOU.cellFormula('Activities!B4')
        };
        expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset, true);
        expectedResultWithOffset['!ref'] = 'A1:D12';

      });

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
      var expectedResultNoOffset;
      var offset = 11;
      var expectedResultWithOffset;

      beforeEach(function() {
        expectedResultNoOffset = {
          '!ref': 'A1:E3',
          A1: IOU.cellValue('id'),
          B1: IOU.cellValue('name'),
          C1: IOU.cellValue('description'),
          D1: IOU.cellValue('duration'),
          E1: IOU.cellValue('Is primary?'),
          A2: IOU.cellValue('epoch1Uri'),
          B2: IOU.cellValue('epoch1label'),
          C2: IOU.cellValue('epoch1comment'),
          D2: IOU.cellValue('P1W'),
          E2: IOU.cellValue(false),
          A3: IOU.cellValue('epoch2Uri'),
          B3: IOU.cellValue('epoch2label'),
          C3: IOU.cellValue('epoch2comment'),
          D3: IOU.cellValue('P1D'),
          E3: IOU.cellValue(true)
        };
        expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
        expectedResultWithOffset['!ref'] = 'A1:E14';
      });
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
      var expectedResultNoOffset;
      var offset = 6;
      var expectedResultWithOffset;

      beforeEach(function() {
        expectedResultNoOffset = {
          '!ref': 'A1:Q4',
          A1: IOU.cellValue('id'),
          B1: IOU.cellValue('title'),
          C1: IOU.cellValue('type'),
          D1: IOU.cellValue('description'),
          E1: IOU.cellValue('drug label'),
          F1: IOU.cellValue('dose type'),
          G1: IOU.cellValue('dose'),
          H1: IOU.cellValue('max dose'),
          I1: IOU.cellValue('unit'),
          J1: IOU.cellValue('periodicity'),
          K1: IOU.cellValue('drug label'),
          L1: IOU.cellValue('dose type'),
          M1: IOU.cellValue('dose'),
          N1: IOU.cellValue('max dose'),
          O1: IOU.cellValue('unit'),
          P1: IOU.cellValue('periodicity'),
          A2: IOU.cellValue('randomizationUri'),
          B2: IOU.cellValue('Randomization'),
          C2: IOU.cellValue('Randomization'),
          D2: IOU.cellValue(undefined),
          A3: IOU.cellValue('fixedSingleDoseUri'),
          B3: IOU.cellValue('Fixed'),
          C3: IOU.cellValue('fixed'),
          D3: IOU.cellValue(undefined),
          E3: IOU.cellFormula('Concepts!B6'),
          F3: IOU.cellValue('fixed'),
          G3: IOU.cellNumber(1),
          H3: IOU.cellNumber(undefined),
          I3: IOU.cellFormula('Concepts!B8'),
          J3: IOU.cellValue('P1D'), //
          A4: IOU.cellValue('combiTreatmentUri'),
          B4: IOU.cellValue('Combination'),
          C4: IOU.cellValue('combination'),
          D4: IOU.cellValue(undefined),
          E4: IOU.cellFormula('Concepts!B6'),
          F4: IOU.cellValue('fixed'),
          G4: IOU.cellNumber(2),
          H4: IOU.cellNumber(undefined),
          I4: IOU.cellFormula('Concepts!B8'),
          J4: IOU.cellValue('P1D'), //
          K4: IOU.cellFormula('Concepts!B7'),
          L4: IOU.cellValue('titrated'),
          M4: IOU.cellNumber(3),
          N4: IOU.cellNumber(4),
          O4: IOU.cellFormula('Concepts!B8'),
          P4: IOU.cellValue('P1D')
        };
        expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
        expectedResultWithOffset['!ref'] = 'A1:Q10';
      });
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
      var measurementMomentSheet;
      var expectedResultNoOffset;
      var expectedResultWithOffset;

      var offset = 5;

      beforeEach(function() {
        measurementMomentSheet = {
          A1: IOU.cellValue('measurementMoment1Uri'),
          A2: IOU.cellValue('measurement moment 1'),
          B1: IOU.cellValue('measurementMoment2Uri'),
          B2: IOU.cellValue('measurement moment 2'),
          C1: IOU.cellValue('measurementMoment3Uri')
        };
        expectedResultNoOffset = {
          '!merges': [],
          '!ref': 'A1:AJ6',
          //headers
          //row 1 (data categories)
          A1: IOU.cellValue('Study Information'),
          I1: IOU.cellValue('Population Information'),
          K1: IOU.cellValue('Arm Information'),
          M1: IOU.cellValue('Measurement Information'),
          //row 2 (variable names)
          M2: IOU.cellFormula('Concepts!B2'),
          U2: IOU.cellFormula('Concepts!B3'),
          Z2: IOU.cellFormula('Concepts!B4'),
          AE2: IOU.cellFormula('Concepts!B5'),
          // row 3 (variable detail headers)
          A3: IOU.cellValue('id'), //row 3
          B3: IOU.cellValue('addis url'),
          C3: IOU.cellValue('title'),
          D3: IOU.cellValue('group allocation'),
          E3: IOU.cellValue('blinding'),
          F3: IOU.cellValue('status'),
          G3: IOU.cellValue('number of centers'),
          H3: IOU.cellValue('objective'),
          I3: IOU.cellValue('indication'),
          J3: IOU.cellValue('eligibility criteria'),
          K3: IOU.cellValue('title'),
          L3: IOU.cellValue('description'),
          M3: IOU.cellValue('variable type'),
          N3: IOU.cellValue('measurement type'),
          O3: IOU.cellValue('measurement moment'),
          P3: IOU.cellValue('sample_size'),
          Q3: IOU.cellValue('count'),
          R3: IOU.cellValue('measurement moment'),
          S3: IOU.cellValue('sample_size'),
          T3: IOU.cellValue('count'),
          U3: IOU.cellValue('variable type'),
          V3: IOU.cellValue('measurement type'),
          W3: IOU.cellValue('measurement moment'),
          X3: IOU.cellValue('male'),
          Y3: IOU.cellValue('female'),
          Z3: IOU.cellValue('variable type'),
          AA3: IOU.cellValue('measurement type'),
          AB3: IOU.cellValue('sample_size'),
          AC3: IOU.cellValue('mean'),
          AD3: IOU.cellValue('standard_deviation'),
          AE3: IOU.cellValue('variable type'),
          AF3: IOU.cellValue('measurement type'),
          AG3: IOU.cellValue('time scale'),
          AH3: IOU.cellValue('measurement moment'),
          AI3: IOU.cellValue('count'),
          AJ3: IOU.cellValue('exposure'),
          // data rows
          // arm 1
          A4: IOU.cellValue(study.label),
          B4: _.merge(IOU.cellValue(studyUrl), {
            l: {
              Target: studyUrl
            }
          }),
          C4: IOU.cellValue(study.comment),
          D4: IOU.cellValue(GROUP_ALLOCATION_OPTIONS[studyInformation.allocation].label),
          E4: IOU.cellValue(BLINDING_OPTIONS[studyInformation.blinding].label),
          F4: IOU.cellValue(STATUS_OPTIONS[studyInformation.status].label),
          G4: IOU.cellNumber(studyInformation.numberOfCenters),
          H4: IOU.cellValue(studyInformation.objective.comment),
          I4: IOU.cellValue(populationInformation.indication.label),
          J4: IOU.cellValue(populationInformation.eligibilityCriteria.label),
          K4: IOU.cellValue(arms[0].label),
          L4: IOU.cellValue(arms[0].comment),
          M4: IOU.cellValue('baseline characteristic'),
          N4: IOU.cellValue('dichotomous'),
          O4: IOU.cellFormula('\'Measurement moments\'!B1'),
          P4: IOU.cellNumber(123),
          Q4: IOU.cellNumber(37),
          R4: IOU.cellFormula('\'Measurement moments\'!C1'),
          S4: IOU.cellNumber(234),
          T4: IOU.cellNumber(73),
          U4: IOU.cellValue('baseline characteristic'),
          V4: IOU.cellValue('categorical'),
          W4: IOU.cellFormula('\'Measurement moments\'!B1'),
          X4: IOU.cellNumber(119),
          Y4: IOU.cellNumber(201),
          Z4: IOU.cellValue('adverse event'),
          AA4: IOU.cellValue('continuous'),
          AE4: IOU.cellValue('adverse event'),
          AF4: IOU.cellValue('survival'),
          AG4: IOU.cellValue('P1Y'),
          AH4: IOU.cellFormula('\'Measurement moments\'!B1'),
          AI4: IOU.cellNumber(123),
          AJ4: IOU.cellNumber(3789),

          // arm 2
          K5: IOU.cellValue(arms[1].label),
          L5: IOU.cellValue(arms[1].comment),
          P5: IOU.cellNumber(321),
          Q5: IOU.cellNumber(42),
          S5: IOU.cellNumber(432),
          T5: IOU.cellNumber(24),
          X5: IOU.cellNumber(301),
          Y5: IOU.cellNumber(401),
          AI5: IOU.cellNumber(321),
          AJ5: IOU.cellNumber(45678),

          // overall population
          K6: IOU.cellValue('Overall population'),
          L6: IOU.cellValue(undefined),
          Q6: IOU.cellNumber(500)
        };
        expectedResultNoOffset['!merges'] = [
          IOU.cellRange(0, 0, 7, 0),
          IOU.cellRange(8, 0, 9, 0),
          IOU.cellRange(10, 0, 11, 0),
          IOU.cellRange(12, 0, 35, 0),
          IOU.cellRange(12, 3, 12, 5),
          IOU.cellRange(13, 3, 13, 5),
          IOU.cellRange(14, 3, 14, 5),
          IOU.cellRange(17, 3, 17, 5),
          IOU.cellRange(12, 1, 19, 1),
          IOU.cellRange(20, 3, 20, 5),
          IOU.cellRange(21, 3, 21, 5),
          IOU.cellRange(22, 3, 22, 5),
          IOU.cellRange(20, 1, 24, 1),
          IOU.cellRange(25, 3, 25, 5),
          IOU.cellRange(26, 3, 26, 5),
          IOU.cellRange(25, 1, 26, 1),
          IOU.cellRange(27, 3, 27, 5),
          IOU.cellRange(28, 3, 28, 5),
          IOU.cellRange(29, 3, 29, 5),
          IOU.cellRange(27, 1, 32, 1),
        ];

        expectedResultNoOffset['!merges'] = expectedResultNoOffset['!merges'].concat(_.map(_.range(0, 10), function(i) {
          return IOU.cellRange(i, 3, i, 3 + arms.length);
        }));

        expectedResultWithOffset = shiftExpectations(expectedResultNoOffset, offset);
        for (var i = 4; i < expectedResultWithOffset['!merges'].length; ++i) {
          expectedResultWithOffset['!merges'][i].s.r += offset;
          expectedResultWithOffset['!merges'][i].e.r += offset;
        }
        expectedResultWithOffset['!ref'] = 'A1:AJ11';
      });

      it('should generate the study data worksheet', function() {
        var result = excelExportUtilService.buildStudyDataSheet(startRows, study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
          populationInformation, variables, conceptsSheet, measurementMomentSheet);
        expect(result).toEqualObjectWithoutKey(expectedResultNoOffset);
      });
      it('should generate the study data worksheet correctly if there is an offset', function() {
        var offsetStartRows = {
          'Study data': offset
        };
        var result = excelExportUtilService.buildStudyDataSheet(offsetStartRows, study, studyInformation, studyUrl, arms, epochs, activities, studyDesign,
          populationInformation, variables, conceptsSheet, measurementMomentSheet);
        expect(result).toEqualObjectWithoutKey(expectedResultWithOffset);
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
      it('should concatenate the !merges property of sheets if any', function() {
        var source = {
          a: 'b',
          '!ref': 'A1:D2',
          '!merges': [IOU.cellRange(0, 1, 0, 3)]
        };
        var target = {
          c: 'd',
          '!ref': 'A1:B5',
          '!merges': [IOU.cellRange(1, 1, 1, 3)]
        };
        var expectedResult = {
          a: 'b',
          c: 'd',
          '!ref': 'A1:D5',
          '!merges': [IOU.cellRange(1, 1, 1, 3), IOU.cellRange(0, 1, 0, 3)]
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
          A1: IOU.cellValue('title'),
          B1: IOU.cellValue('ADDIS url'),
          C1: IOU.cellValue('description'),
          A2: IOU.cellValue(datasetWithCoordinates.title),
          B2: IOU.cellLink(datasetWithCoordinates.url),
          C2: IOU.cellValue(datasetWithCoordinates.comment),
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
          A1: IOU.cellValue('id'),
          B1: IOU.cellValue('label'),
          C1: IOU.cellValue('type'),
          A2: IOU.cellValue('http://unitConceptUri'),
          B2: IOU.cellValue('milligram'),
          C2: IOU.cellValue('Unit'),
          A3: IOU.cellValue('http://drugConceptUri'),
          B3: IOU.cellValue('drug 1'),
          C3: IOU.cellValue('Drug'),
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
              A34: IOU.cellFormula('\'Study data\'!A5')
            },
            'Study design': {
              hoo: 'dar',
              A555: IOU.cellFormula('\'Study data\'!A5')
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

  });
});