define(['angular', 'angular-mocks', 'filters'], function () {
  describe("The activity type filter", function () {
    var activityTypeFilter;

    beforeEach(module('addis.filters'));

    beforeEach(inject(function($filter) {
      activityTypeFilter = $filter('activityTypeFilter');
    }));

    it("should the remove part before the hashtag token en strip the word 'Activity' from the end of the uri", function() {
      var uri = 'http://somethings/otherthing/hereiscomes#resultActivity';
      expect(activityTypeFilter(uri)).toEqual('result');
    });

    it("should pass though a empty input", function() {
      expect(activityTypeFilter(undefined)).toEqual(undefined);
    });

  });
});
