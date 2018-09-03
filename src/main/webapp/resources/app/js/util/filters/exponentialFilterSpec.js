define(['angular-mocks'], function (angularMocks) {
  describe('The exponential filter', function () {
    var exponentialFilter;

    beforeEach(angular.mock.module('trialverse.util'));

    beforeEach(inject(function($filter) {
      exponentialFilter = $filter('exponentialFilter');
    }));

    it('should the correct numeric value', function() {
      expect(exponentialFilter('1e0')).toEqual(1);
      expect(exponentialFilter('1e-0')).toEqual(1);
      expect(exponentialFilter('1e-1')).toEqual(0.1);
      expect(exponentialFilter('1e-2')).toEqual(0.01);
      expect(exponentialFilter('1e1')).toEqual(10);
      expect(exponentialFilter('1e2')).toEqual(100);
      expect(exponentialFilter('1e3')).toEqual(1000);
      expect(exponentialFilter('1e4')).toEqual(10000);
    });
  });
});
