'use strict';
define(['angular', 'angular-mocks', '../filters'], function() {
  describe("The categorical measurement filter", function() {
    var categoricalFilter;

    beforeEach(angular.mock.module('addis.filters'));

    beforeEach(inject(function($filter) {
      categoricalFilter = $filter('categoricalFilter');
    }));

    it('should convert a list of values to a slash-separated string of key=value pairs', function() {
      var values = [{key1: 20},{'key 2': 256},{'key tres': 37}   ];

      expect(categoricalFilter(values)).toEqual('key1=20 / key 2=256 / key tres=37');
    });

  });
});
