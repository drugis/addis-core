'use strict';
define(['angular-mocks', './measurementMoment'], function() {
  describe('the measurement moment service', function() {
    var
      studyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery', 'doNonModifyingQuery']),
      epochService = jasmine.createSpyObj('EpochService', ['queryItems']),
      uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate']),
      measurementMomentService;

    beforeEach(angular.mock.module('trialverse.measurementMoment'));

    beforeEach(function() {
      angular.mock.module('trialverse.study', function($provide) {
        $provide.value('StudyService', studyService);
      });
      angular.mock.module('trialverse.epoch', function($provide) {
        $provide.value('EpochService', epochService);
      });
      angular.mock.module('trialverse.util', function($provide) {
        $provide.value('UUIDService', uuidServiceMock);
      });
    });

    beforeEach(inject(function(MeasurementMomentService) {
      measurementMomentService = MeasurementMomentService;
      uuidServiceMock.generate.and.returnValue('generatedUUID');
    }));

    describe('generateLabel', function() {
      describe('should return an empty string when', function() {
        it('there is no data', function() {
          var measurementMoment = {};
          expect(measurementMomentService.generateLabel(measurementMoment)).toEqual('');
        });
        it('there is only an epoch', function() {
          var measurementMoment = {
            epoch: {
              label: 'test'
            }
          };
          expect(measurementMomentService.generateLabel(measurementMoment)).toEqual('');
        });
        it('there is an epoch and an offset but no anchor relation', function() {
          var measurementMoment = {
            epoch: {
              label: 'test'
            },
            offset: 'PT3H'
          };
          expect(measurementMomentService.generateLabel(measurementMoment)).toEqual('');
        });
        it('there is an epoch and anchor relation but no offset', function() {
          var measurementMoment = {
            epoch: {
              label: 'test'
            },
            relativeToAnchor: 'ontology:anchorEpochStart'
          };
          expect(measurementMomentService.generateLabel(measurementMoment)).toEqual('');
        });
      });
      it('should work for zero duration', function() {
        var measurementMoment = {
          epoch: {
            uri: 'epochUri',
            label: 'main phase'
          },
          relativeToAnchor: 'ontology:anchorEpochStart',
          offset: 'PT0S'
        };

        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('At start of main phase');

        measurementMoment.relativeToAnchor = 'http://trials.drugis.org/ontology#anchorEpochEnd';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('At end of main phase');
      });

      it('should work for hours', function() {
        var measurementMoment = {
          epoch: {
            uri: 'epochUri',
            label: 'main phase'
          },
          relativeToAnchor: 'ontology:anchorEpochStart',
          offset: 'PT3H'
        };

        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('3 hour(s) from start of main phase');

        measurementMoment.relativeToAnchor = 'http://trials.drugis.org/ontology#anchorEpochEnd';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('3 hour(s) from end of main phase');

        measurementMoment.offset = 'PT12H';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('12 hour(s) from end of main phase');
      });

      it('should work for days', function() {
        var measurementMoment = {
          epoch: {
            uri: 'epochUri',
            label: 'main phase'
          },
          relativeToAnchor: 'ontology:anchorEpochStart',
          offset: 'P3D'
        };

        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('3 day(s) from start of main phase');

        measurementMoment.relativeToAnchor = 'http://trials.drugis.org/ontology#anchorEpochEnd';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('3 day(s) from end of main phase');

        measurementMoment.offset = 'P12D';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('12 day(s) from end of main phase');
      });
    });

  });
});
