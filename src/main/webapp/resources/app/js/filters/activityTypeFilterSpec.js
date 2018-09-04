define(['../filters'], function () {
  describe("The activity type filter", function () {
    var activityTypeFilter;

    beforeEach(angular.mock.module('addis.filters'));

    beforeEach(inject(function($filter) {
      activityTypeFilter = $filter('activityTypeFilter');
    }));

    it("should strip the word 'Activity' from the end of the input", function() {
      var input = 'dfsdfd df ff121432ABCsafaf resultActivity';
      expect(activityTypeFilter(input)).toEqual('dfsdfd df ff121432ABCsafaf result');
    });

    it("should pass though a empty input", function() {
      expect(activityTypeFilter(undefined)).toEqual(undefined);
    });

    it("should pass though a input not ending with Activity", function() {
      var input = 'this string ends with something else';
      expect(activityTypeFilter(undefined)).toEqual(undefined);
    });

  });
});
