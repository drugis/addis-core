'use strict';
define(['angular', 'angular-mocks'], function () {
  describe('uuid service', function () {
    beforeEach(module('trialverse.util'));

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
   
  });
});
