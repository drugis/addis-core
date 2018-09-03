'use strict';
define(['angular-mocks', '../filters'], function (angularMocks) {
  describe("The anchorEpoch filter", function () {
    var anchorEpochFilter;

    beforeEach(angular.mock.module('addis.filters'));

    beforeEach(inject(function($filter) {
      anchorEpochFilter = $filter('anchorEpochFilter');
    }));

    it("should strip the 'anchorEpoch' part of the string", function() {
      var input = 'anchorEpochStart';
      expect(anchorEpochFilter(input)).toEqual('Start');
    });

  });
});
