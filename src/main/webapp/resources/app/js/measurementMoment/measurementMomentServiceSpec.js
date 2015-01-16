'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the measurement moment service', function() {
    var
      sparqlResource = jasmine.createSpyObj('SparqlResource', ['get']),
      studyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery', 'doNonModifyingQuery']),
      q, rootScope,
      nonModifyingQueryPromise, queryResourcePromise,
      measurementMomentService;

    beforeEach(module('trialverse.measurementMoment'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', studyService);
        $provide.value('SparqlResource', sparqlResource);
      });
    });

    beforeEach(inject(function($q, $rootScope) {
      q = $q;
      rootScope = $rootScope;
      queryResourcePromise = q.defer();
      nonModifyingQueryPromise = q.defer();
      studyService.doNonModifyingQuery.and.returnValue(nonModifyingQueryPromise);
      sparqlResource.get.and.returnValue({
        $promise: queryResourcePromise.promise
      });
    }));

    beforeEach(inject(function(MeasurementMomentService) {
      measurementMomentService = MeasurementMomentService;
    }));

    describe('queryItems', function() {
      it('should query the measurement moments', function() {
        var result = measurementMomentService.queryItems();
        var queryResult = 'any string 2';

        queryResourcePromise.resolve('any string 1');
        nonModifyingQueryPromise.resolve(queryResult);
        rootScope.$digest();

        expect(result.$$state.value.promise.$$state.value).toEqual(queryResult);
      });
    });

    describe('generateLabel', function() {
      it('should work for zero duration', function() {
        var measurementMoment = {
          epoch: {
            uri: 'epochUri',
            label: 'main phase'
          },
          relativeToAnchor: '<http://trials.drugis.org/ontology#anchorEpochStart>',
          offset: 'PT0H'
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
            label: 'main phase'
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
            label: 'main phase'
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
