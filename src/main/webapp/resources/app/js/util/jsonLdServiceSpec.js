'use strict';
define(['angular', 'angular-mocks'], function () {
  describe('jsonLd service', function () {
    beforeEach(module('trialverse.util'));

    describe('rewriteAtIds', function() {
      it('should exist', inject(function(JsonLdService) {
        expect(JsonLdService).not.toBe(null);
      }));

      it('should make uuids from "@id" properties', inject(function(JsonLdService) {
        var jsonLdObject = {};
        jsonLdObject['@id'] = 'namespace:uuid';
        var input = [].push(jsonLdObject);
        var expectedResult = {uuid: 'uuid'};
        expectedResult['@id'] = 'namespace:uuid';

        var result = JsonLdService.rewriteAtIds([jsonLdObject]);

        expect(result[0].uuid).toBe('uuid');
        expect(result[0]['@id']).toBe('namespace:uuid');
      }));
    });
   
  });
});
