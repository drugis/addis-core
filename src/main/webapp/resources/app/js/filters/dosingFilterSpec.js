define(['angular-mocks', '../filters'], function () {
  describe("The dosing filter", function () {
    var dosingFilter;

    beforeEach(angular.mock.module('addis.filters'));

    beforeEach(inject(function($filter) {
      dosingFilter = $filter('dosingFilter');
    }));

    it("should return 'Fixed' when tha input is empty", function() {
      expect(dosingFilter('')).toEqual('Fixed');
    });

    it("should return tha input when the input is not empty", function() {
      expect(dosingFilter('yo this is not empty')).toEqual('yo this is not empty');
    });

  });
});
