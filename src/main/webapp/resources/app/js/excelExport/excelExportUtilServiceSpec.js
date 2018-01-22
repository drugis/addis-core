'use strict';
define(['lodash', 'angular', 'angular-mocks'], function(_) {
  describe('the excel export util service', function() {
    var rootScope, q;
    var excelExportUtilService;
    var resultsService = jasmine.createSpyObj('ResultsService', ['queryResults']);
    var GROUP_ALLOCATION_OPTIONS = {};
    var BLINDING_OPTIONS = {};
    var STATUS_OPTIONS = {};
    var promise1;
    var promise2;
    var promise3;
    var promise1defer;
    var promise2defer;
    var promise3defer;

    beforeEach(function() {
      module('addis.excelExport', function($provide) {
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
      it('should generate the conceps sheet', function() {
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

        var expectedResult = {
          '!ref': 'A1:E4',
          A1: cellValue('id'),
          B1: cellValue('label'),
          C1: cellValue('type'),
          D1: cellValue('dataset concept uri'),
          E1: cellValue('multiplier'),
          A2: cellValue('drugUri'),
          B2: cellValue('drug'),
          C2: cellValue('drug'),
          D2: cellValue(undefined),
          E2: cellValue(undefined),
          A3: cellValue('variableUri'),
          B3: cellValue('variable'),
          C3: cellValue('variable'),
          D3: cellValue('datasetVariableUri'),
          E3: cellValue(undefined),
          A4: cellValue('unitUri'),
          B4: cellValue('mg'),
          C4: cellValue('unit'),
          D4: cellValue('datasetGramUri'),
          E4: cellValue(0.001),
        };

        var result = excelExportUtilService.buildConceptsSheet(studyConcepts);

        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildMeasurementMomentSheet', function() {
      it('should generate the measurement moment sheet', function() {
        var measurementMoments = [{
          uri: 'measurementMomentUri',
          label: 'name',
          epochUri: 'epochRef',
          relativeToAnchor: 'ontology:anchorEpochStart',
          offset: 'offset'
        }];
        var epochSheet = {
          B2: {
            v: 'epochRef'
          }
        };
        var result = excelExportUtilService.buildMeasurementMomentSheet(measurementMoments, epochSheet);
        var expectedResult = {
          '!ref': 'A1:E2',
          A1: cellValue('id'),
          B1: cellValue('name'),
          C1: cellValue('epoch'),
          D1: cellValue('from'),
          E1: cellValue('offset'),
          A2: cellValue('measurementMomentUri'),
          B2: cellValue('name'),
          C2: {
            f: '=Epochs!B2'
          },
          D2: cellValue('start'),
          E2: cellValue('offset')
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildStudyDesignSheet', function() {
      it('should build the study design table sheet', function() {
        var epochs = [{
          uri: 'epoch1Uri'
        }, {
          uri: 'epoch2Uri'
        }];
        var arms = [{
          armURI: 'arm1Uri',
          label: 'arm 1'
        }, {
          armURI: 'arm2Uri',
          label: 'arm 2'
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
            activityUri: 'treatment1Uri'
          }, {
            epochUri: 'epoch2Uri',
            armUri: 'arm2Uri',
            activityUri: 'treatment2Uri'
          }
        ];
        var epochSheet = {
          A2: cellValue('epoch1Uri'),
          A3: cellValue('epoch2Uri')
        };
        var activitiesSheet = {
          A2: cellValue('randomizationUri'),
          A3: cellValue('treatment1Uri'),
          A4: cellValue('treatment2Uri')
        };
        var studyDataSheet = {
          A2: cellValue('arm 1'),
          A3: cellValue('arm 2')
        };

        var expectedResult = {
          '!ref': 'A1:D3',
          A1: cellValue('arm'),
          B1: {
            f: '=Epochs!B2'
          },
          C1: {
            f: '=Epochs!B3'
          },
          A2: {
            f: '=\'Study Data\'!A2'
          },
          B2: {
            f: '=Activities!B2'
          },
          C2: {
            f: '=Activities!B3'
          },
          A3: {
            f: '=\'Study Data\'!A3'
          },
          B3: {
            f: '=Activities!B2'
          },
          C3: {
            f: '=Activities!B4'
          }
        };

        var result = excelExportUtilService.buildStudyDesignSheet(epochs, arms, studyDesign, epochSheet, activitiesSheet, 
          studyDataSheet);

        expect(result).toEqual(expectedResult);

      });
    });

    function cellValue(value) {
      return {
        v: value
      };
    }


  });
});