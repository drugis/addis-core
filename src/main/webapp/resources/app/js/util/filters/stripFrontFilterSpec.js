define(['angular-mocks'], function (angularMocks) {
  describe("The stripFront filter", function () {
    var stripFrontFilter;

    beforeEach(angular.mock.module('trialverse.util'));

    beforeEach(inject(function($filter) {
      stripFrontFilter = $filter('stripFrontFilter');
    }));

    it("should remove nothing if the frontStr is empty", function() {
      expect(stripFrontFilter('string with no token', '')).toEqual('string with no token');
    });

    it('should remove the first instance of frontStr from the input', function() {
      expect(stripFrontFilter('hier is het beginenderest', 'hier is het begin')).toEqual('enderest');
    });

    it('should work if the frontStr is equal to the input', function() {
      expect(stripFrontFilter('hetse lfde', 'hetse lfde')).toEqual('');
    });

    it('should work if the both strings are empty', function() {
      expect(stripFrontFilter('', '')).toEqual('');
    });

    it('should do nothing if frontstr is longer than the input', function() {
      expect(stripFrontFilter('aa', 'aaaa')).toEqual('aa');
    });

    it('should do nothing if the frontstr is not actually the beginning of the input', function() {
      expect(stripFrontFilter('bbbbbbb', 'aa')).toEqual('bbbbbbb');
    });

    it('should handle nulls', function() {
      expect(stripFrontFilter(null, 'aaaa')).toEqual(null);
      expect(stripFrontFilter('aaaaa', null)).toEqual('aaaaa');
    });

  });
});
