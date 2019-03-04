define(['angular-mocks'], function (angularMocks) {
  describe('The duration filter', function () {
    var durationFilter;

    beforeEach(angular.mock.module('trialverse.util'));

    beforeEach(inject(function($filter) {
      durationFilter = $filter('durationFilter');
    }));

    it('should pass though undefined durations', function() {
      expect(durationFilter(undefined)).toEqual(undefined);
    });

    it('should humanize a valid duration', function() {
      expect(durationFilter('P1D')).toEqual('1 day(s)');
    });

    it('should replace empty periods with null', function() { // is this what we want ? :s
      expect(durationFilter('P0D')).toEqual(null);
      expect(durationFilter('-P0D')).toEqual(null);
    });

    it('should replace 0 seconds with "instantaneous"', function() {
      expect(durationFilter('PT0S')).toEqual('instantaneous');
    });

    it('should render periods >24h as days', function() {
      expect(durationFilter('P60D')).toEqual('60 day(s)');
      expect(durationFilter('P8W')).toEqual('56 day(s)');
      expect(durationFilter('PT1H')).toEqual('1 hour(s)');
    });

  });
});
