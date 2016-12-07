'use strict';
define(['angular-mocks'], function() {
  describe('the project service', function() {
    var projectService;

    beforeEach(module('addis.project'));

    beforeEach(inject(function(ProjectService) {
      projectService = ProjectService;
    }));
    describe('checkforDuplicateName', function() {
      it('should check for duplicate names', function() {
        var itemList = [{
          name: 'item1',
          id: '12'
        }, {
          name: 'item2',
          id: '34'
        }];
        var itemToCheck = {
          name: 'item2',
          id: '56'
        };

        var result = projectService.checkforDuplicateName(itemList, itemToCheck);
        expect(result).toBeTruthy();
      });
      it('should return false when no duplicate is found',function() {
        var itemList = [{
          name: 'item1',
          id: '12'
        }, {
          name: 'item2',
          id: '34'
        }];
        var itemToCheck = {
          name: 'item3',
          id: '56'
        };
        var result = projectService.checkforDuplicateName(itemList, itemToCheck);
        expect(result).toBeFalsy();
      });
      it('should return false there is no duplicate name, but the id already exists',function() {
        var itemList = [{
          name: 'item1',
          id: '12'
        }, {
          name: 'item2',
          id: '34'
        }];
        var itemToCheck = {
          name: 'item3',
          id: '12'
        };
        var result = projectService.checkforDuplicateName(itemList, itemToCheck);
        expect(result).toBeFalsy();
      });
    });

    describe('buildCovariateUsage', function() {
      it('should build a map keyed by covariate ID where the values are a list of analyses including that covariate', function() {
        var analyses = [{
          title: 'analysis 1',
          includedCovariates: [{
            covariateId: 37
          }]
        }, {
          title: 'analysis 2',
          includedCovariates: [{
            covariateId: 42
          }]
        }, {
          includedCovariates: []
        }];
        var covariates = [{
          id: 37
        }, {
          id: 42
        }, {
          id: 1337
        }];
        var expectedResult = {
          37: ['analysis 1'],
          42: ['analysis 2'],
          1337: []
        };

        var result = projectService.buildCovariateUsage(analyses, covariates);

        expect(result).toEqual(expectedResult);
      });
    });

  });
});
