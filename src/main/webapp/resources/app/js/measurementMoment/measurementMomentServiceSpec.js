'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the measurement moment service', function() {
    var
      sparqlResource = jasmine.createSpyObj('SparqlResource', ['get']),
      studyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery', 'doNonModifyingQuery']),
      epochService = jasmine.createSpyObj('EpochService', ['queryItems']),
      q, rootScope,
      nonModifyingQueryPromise, queryResourcePromise, epochQueryPromise,
      measurementMomentService;

    beforeEach(module('trialverse.measurementMoment'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', studyService);
        $provide.value('EpochService', epochService);
        $provide.value('SparqlResource', sparqlResource);
      });
    });

    beforeEach(inject(function($q, $rootScope) {
      q = $q;
      rootScope = $rootScope;
      queryResourcePromise = q.defer();
      nonModifyingQueryPromise = q.defer();
      epochQueryPromise = q.defer();

      epochService.queryItems.and.returnValue(epochQueryPromise.promise);
      studyService.doNonModifyingQuery.and.returnValue(nonModifyingQueryPromise.promise);
      sparqlResource.get.and.returnValue(queryResourcePromise.promise);
    }));

    beforeEach(inject(function(MeasurementMomentService) {
      measurementMomentService = MeasurementMomentService;
    }));

    describe('queryItems', function() {
      it('should query the measurement moments', function() {
        var result = measurementMomentService.queryItems();
        var queryResult = [{
          epochUri: {
            value: 1
          }
        }, {
          epochUri: {
            value: 2
          }
        }];
        var expectedResult = [{
          epochUri: {
            value: 1
          },
          epoch: {
            uri: {value: 1}
          }
        }, {
          epochUri: {
            value: 2
          },
          epoch: {
            uri: {value: 2}
          }
        }];
        var epochs = [{
          uri: {
            value: 2
          }
        }, {
          uri: {
            value: 1
          }
        }];
        epochQueryPromise.resolve(epochs);
        queryResourcePromise.resolve('any string 1');
        nonModifyingQueryPromise.resolve(queryResult);
        rootScope.$digest();

        expect(result.$$state.value).toEqual(expectedResult);
      });
    });

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
            relativeToAnchor: '<http://trials.drugis.org/ontology#anchorEpochStart>'
          };
          expect(measurementMomentService.generateLabel(measurementMoment)).toEqual('');
        });
      });
      it('should work for zero duration', function() {
        var measurementMoment = {
          epoch: {
            uri: 'epochUri',
            label: {
              value: 'main phase'
            }
          },
          relativeToAnchor: '<http://trials.drugis.org/ontology#anchorEpochStart>',
          offset: 'PT0S'
        };

        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('At start of main phase');

        measurementMoment.relativeToAnchor = '<http://trials.drugis.org/ontology#anchorEpochEnd>';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('At end of main phase');
      });

      it('should work for hours', function() {
        var measurementMoment = {
          epoch: {
            uri: 'epochUri',
            label: {
              value: 'main phase'
            }
          },
          relativeToAnchor: '<http://trials.drugis.org/ontology#anchorEpochStart>',
          offset: 'PT3H'
        };

        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('3 hour(s) from start of main phase');

        measurementMoment.relativeToAnchor = '<http://trials.drugis.org/ontology#anchorEpochEnd>';
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
            label: {
              value: 'main phase'
            }
          },
          relativeToAnchor: '<http://trials.drugis.org/ontology#anchorEpochStart>',
          offset: 'P3D'
        };

        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('3 day(s) from start of main phase');

        measurementMoment.relativeToAnchor = '<http://trials.drugis.org/ontology#anchorEpochEnd>';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('3 day(s) from end of main phase');

        measurementMoment.offset = 'P12D';
        expect(measurementMomentService.generateLabel(measurementMoment))
          .toEqual('12 day(s) from end of main phase');
      });
    });

  });
});
