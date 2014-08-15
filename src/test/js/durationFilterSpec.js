define(['angular', 'angular-mocks', 'filters'], function () {
  describe("The duration filter", function () {
    var durationFilter;

    beforeEach(module('addis.filters'));

    beforeEach(inject(function($filter) {
      durationFilter = $filter('durationFilter');
    }));

    it("should pass though undefined durations", function() {
      expect(durationFilter(undefined)).toEqual(undefined);
    });

    it("should humanize a valid duration", function() {
      expect(durationFilter("P1D")).toEqual("a day");
      expect(durationFilter("-P0D")).toEqual("a few seconds"); // is this what we want ? :s
    });

  });
});