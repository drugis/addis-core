'use strict';
define(['angular', 'angular-mocks', '../filters'], function () {
  describe("The ontology filter", function () {
    var ontologyFilter;

    beforeEach(angular.mock.module('addis.filters'));

    beforeEach(inject(function($filter) {
      ontologyFilter = $filter('addisOntologyFilter');
    }));

    it("should strip strip everything up to and including the hash sign from the string", function() {
      var input = 'andThereItComes#boom';
      expect(ontologyFilter(input)).toEqual('boom');
    });

  });
});
