'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('unit service', function() {
    var unitService;

    beforeEach(module('trialverse.intervention'));

    beforeEach(inject(function(UnitNamesService) {
      unitService = UnitService;
    }));

    describe('get', function() {
      it('should query and transform the response', function() {
        var userUid = 'user',
          datasetUuid = 'data-s3t',
          datasetVersionUuid = 'vers-i0n';
        var result = unitService.get(userUid, datasetUuid, datasetVersionUuid);
        expect(result).toEqual([{
          unitName: 'milligram'
        }, {
          unitName: 'milliliter'
        }]);
      });
    });

  });
});
