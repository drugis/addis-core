'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the non-conformant measurements table service', function() {

    var nonConformantMeasurementTableService;
    var resultsTableService = jasmine.createSpyObj('ResultsTableService', ['createInputColumns']);

    beforeEach(angular.mock.module('trialverse.results', function($provide) {
      $provide.value('ResultsTableService', resultsTableService);
    }));

    beforeEach(inject(function(NonConformantMeasurementTableService) {
      nonConformantMeasurementTableService = NonConformantMeasurementTableService;
    }));

    describe('mapResultsByLabelAndGroup', function() {
      it('should map results by label and group', function() {
        var arms = [{
          label: 'arm 1',
          armURI: 'http://arms/arm1'
        }];

        var groups = [];

        var resultValuesObjects = [{
          comment: 'comment1',
          uri: 'uri 1',
          armUri: 'http://arms/arm1',
          instance: 'instance1'
        }, {
          comment: 'comment2',
          uri: 'uri 2',
          armUri: 'http://arms/arm1',
          instance: 'instance1'
        }, {
          comment: 'comment3',
          uri: 'uri 3',
          armUri: 'http://arms/arm1',
          instance: 'instance1'
        }];

        var expectedResult = {
          comment1: {
            'http://arms/arm1': {
              group: arms[0],
              results: [resultValuesObjects[0]]
            }
          },
          comment2: {
            'http://arms/arm1': {
              group: arms[0],
              results: [resultValuesObjects[1]]
            }
          },
          comment3: {
            'http://arms/arm1': {
              group: arms[0],
              results: [resultValuesObjects[2]]
            }
          }
        };

        var result = nonConformantMeasurementTableService.mapResultsByLabelAndGroup(arms, groups, resultValuesObjects);

        expect(result).toEqual(expectedResult);
      });
    });

    describe('createInputRows', function() {
      var resultRows, expectedRow;

      beforeEach(function() {
        var arm ={
          label: 'arm 1',
          armURI: 'http://arms/arm1'
        };
        var resultsByLabelAndGroup = {
          comment1: {
            'http://arms/arm1': {
              group: arm,
              results: [{
                comment: 'comment1',
                uri: 'uri 1',
                armUri: 'http://arms/arm1',
                instance: 'instance1'
              }]
            }
          },
          comment2: {
            'http://arms/arm1': {
              group: arm,
              results: [{
                comment: 'comment2',
                uri: 'uri 2',
                armUri: 'http://arms/arm1',
                instance: 'instance1'
              }]
            }
          },
          comment3: {
            'http://arms/arm1': {
              group: arm,
              results: [{
                comment: 'comment3',
                uri: 'uri 3',
                armUri: 'http://arms/arm1',
                instance: 'instance1'
              }]
            }
          }
        };

        var variable = {
          label: 'foo'
        };
        expectedRow = {
          variable: variable,
          group: {
            'label': 'arm 1',
            'armURI': 'http://arms/arm1'
          },
          label: 'comment1',
          numberOfGroups: 1,
          inputColumns: undefined,
          measurementInstanceList: ['instance1']
        };

        resultRows = nonConformantMeasurementTableService.createInputRows(variable, resultsByLabelAndGroup);
      });

      it('should build te non-conformant table rows', function() {
        expect(resultRows.length).toEqual(3);
        expect(resultRows[0]).toEqual(expectedRow);
        expect(resultsTableService.createInputColumns).toHaveBeenCalled();
      });


    });


  });
});
