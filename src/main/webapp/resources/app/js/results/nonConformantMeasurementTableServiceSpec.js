'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the non-conformant measurements table service', function() {

    var nonConformantMeasurementTableService;
    var resultsTableService = jasmine.createSpyObj('ResultsTableService', ['createInputColumns']);

    beforeEach(module('trialverse.results', function($provide) {
      $provide.value('ResultsTableService', resultsTableService);
    }));

    beforeEach(inject(function(NonConformantMeasurementTableService) {
      nonConformantMeasurementTableService = NonConformantMeasurementTableService;
    }));

    describe('createInputRows', function() {
      var resultRows, variable, arms, resultValuesObjects, expectedRow;

      beforeEach(function() {

        variable = {
          measuredAtMoments: [{
            uri: 'uri 1'
          }, {
            uri: 'uri 3'
          }]
        };

        arms = [{
          label: 'arm 1',
          armURI: 'http://arms/arm1'
        }];

        resultValuesObjects = [{
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

        expectedRow = {
          variable: {
            measuredAtMoments: [{
              'uri': 'uri 1'
            }, {
              'uri': 'uri 3'
            }]
          },
          group: {
            'label': 'arm 1',
            'armURI': 'http://arms/arm1',
            results: [{
              'comment': 'comment3',
              'uri': 'uri 3',
              'armUri': 'http://arms/arm1',
              'instance': 'instance1'
            }]
          },
          label: 'comment1',
          numberOfGroups: 1,
          inputColumns: undefined,
          measurementInstanceList: ['instance1']
        };

        resultRows = nonConformantMeasurementTableService.createInputRows(variable, arms, [], resultValuesObjects);
      });

      it('should build te non-conformant table rows', function() {
        expect(resultRows.length).toEqual(3);
        expect(resultRows[0]).toEqual(expectedRow);
        expect(resultsTableService.createInputColumns).toHaveBeenCalled();
      });


    });


  });
});
