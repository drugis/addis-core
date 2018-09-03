define(['angular-mocks', '../../filters'], function (angularMocks) {
  describe("The duration offset filter", function () {
    var durationOffsetFilter;

    beforeEach(angular.mock.module('trialverse.util'));

    beforeEach(inject(function($filter) {
      durationOffsetFilter = $filter('durationOffsetFilter');
    }));

    it("should pass though undefined durations", function() {
      expect(durationOffsetFilter(undefined)).toEqual(undefined);
    });

    it("should add ' after ' a positive duration ", function() {
      expect(durationOffsetFilter("P1D")).toEqual("1 day(s) after ");
    });

    it("should add ' before ' a negative duration ", function() {
      expect(durationOffsetFilter("P1D")).toEqual("1 day(s) after ");
    });

    it("should replace empty periods with and emply string ", function() { // is this what we want ? :s
      expect(durationOffsetFilter("P0D")).toEqual('');
      expect(durationOffsetFilter("-P0D")).toEqual(''); 
    });

  });
});