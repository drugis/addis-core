'use strict';
define(['angular-mocks', './util'], function(angularMocks) {

  describe('the sanitize service', function() {

    beforeEach(angular.mock.module('trialverse.util'));

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