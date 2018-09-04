define(['angular', 'angular-mocks', '../filters'], function () {
  describe("The splitOnToken filter", function () {
    var splitOnTokenFilter;

    beforeEach(angular.mock.module('addis.filters'));

    beforeEach(inject(function($filter) {
      splitOnTokenFilter = $filter('splitOnTokenFilter');
    }));

    it("should split a list of token seperated items", function() {
      expect(splitOnTokenFilter('a,b,c', ',')).toEqual(['a', 'b', 'c']);
    });

    it("should split a list of token seperated items and trim whitespace", function() {
      expect(splitOnTokenFilter('a, b, c', ',')).toEqual(['a', 'b', 'c']);
    });

    it("should pass though a string with no token", function() {
      expect(splitOnTokenFilter('string with no token', ',')).toEqual(['string with no token']);
    });

    it("should pass though a string with no token", function() {
      expect(splitOnTokenFilter(null)).toEqual(null);
    });

  });
});
