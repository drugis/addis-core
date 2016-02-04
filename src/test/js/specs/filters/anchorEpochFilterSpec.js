'use strict';
define(['angular', 'angular-mocks', 'filters'], function () {
  describe("The anchorEpoch filter", function () {
    var anchorEpochFilter;

    beforeEach(module('addis.filters'));

    beforeEach(inject(function($filter) {
      anchorEpochFilter = $filter('anchorEpochFilter');
    }));

    it("should strip the 'anchorEpoch' part of the string", function() {
      var input = 'anchorEpochStart';
      expect(anchorEpochFilter(input)).toEqual('Start');
    });

  });
});
