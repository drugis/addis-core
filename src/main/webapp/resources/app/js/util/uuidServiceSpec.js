'use strict';
define(['angular-mocks'], function (angularMocks) {
  describe('uuid service', function () {
    beforeEach(angular.mock.module('trialverse.util'));

    describe('generate', function() {
      it('should exist', inject(function(UUIDService) {
        expect(UUIDService).not.toBe(null);
      }));

      it('should return a random-indicated UUID', inject(function(UUIDService) {
        var uuid = UUIDService.generate();
        expect(uuid[14]).toBe('4');
        expect('ab89').toContain(uuid[19]);
      }));
    });
    describe('generate graph uri', function() {
      it('should generate a random graph uri', inject(function(UUIDService) {
        var studyUri = UUIDService.generateGraphUri();
        expect(studyUri.slice(0,32)).toEqual('http://trials.drugis.org/graphs/');
        expect(studyUri.slice(32).length).toEqual(36);
      }));
    });

  });
});
