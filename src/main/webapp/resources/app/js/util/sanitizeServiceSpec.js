'use strict';
define(['angular', 'angular-mocks'], function() {

  describe('the sanitize service', function() {

    beforeEach(module('trialverse.util'));

    it('should escape linefeeds', inject(function(SanitizeService) {
      var testStr = 'hello\ni am cool[]';
      expect(SanitizeService.sanitizeStringLiteral(testStr)).toEqual('hello\\ni am cool[]');
    }));

    it('should not escape forwards slashes', inject(function(SanitizeService) {
      var testStr = 'hello/i am cool[]';
      expect(SanitizeService.sanitizeStringLiteral(testStr)).toEqual('hello/i am cool[]');
    }));

  });

});