'use strict';
define(['angular', 'angular-mocks', '../filters'], function () {
  describe('Filter: ownProjectsFilter', function () {
    var mockProjects = [
      {owner: {id: 1}, name: 'testName1'},
      {owner: {id: 1}, name: 'testName2'},
      {owner: {id: 2}, name: 'testName2'},
      {owner: {id: 2}, name: 'testName3'},
      {owner: {id: 3}, name: 'testName4'}
    ],
    ownProjectsFilter;


    beforeEach(angular.mock.module('addis.filters'));

    beforeEach(inject(function($filter) {
      ownProjectsFilter = $filter('ownProjectsFilter');
    }));

    it('should return the list of projects owned by a specific user', function() {
      expect(ownProjectsFilter(mockProjects, 1).length).toEqual(2);
      expect(ownProjectsFilter(mockProjects, 2, false).length).toEqual(2);
    });

    it('should return the list of not-owned projects by a specific user', function() {
      expect(ownProjectsFilter(mockProjects, 1, true).length).toEqual(3);
    });

    it('should deal gracefully with nonexistent users', function() {
      expect(ownProjectsFilter(mockProjects, 12).length).toEqual(0);
    });
  });
});
