'use strict';
define(['lodash', 'angular-mocks'], function(_) {
  describe('the project service', function() {
    var projectService;

    beforeEach(angular.mock.module('addis.project'));

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
      it('should return false when no duplicate is found', function() {
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
      it('should return false there is no duplicate name, but the id already exists', function() {
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

    describe('buildInterventionUsage', function() {
      it('should build a usage map of the interventions', function() {
        var intervention1 = {
          id: 1,
          type: 'simple'
        };
        var intervention2 = {
          id: 2,
          type: 'simple'
        };
        var intervention3 = {
          id: 3,
          name: '1 plus 2',
          type: 'combination',
          interventionIds: [1, 2]
        };
        var intervention4 = {
          id: 4,
          name: '1 or 2',
          type: 'class',
          interventionIds: [1, 2, 3]
        };
        var interventions = [intervention1, intervention2, intervention3, intervention4];
        var analyses = [{
          title: 'analysis 1',
          interventionInclusions: [{
            interventionId: intervention1.id
          }]
        }, {
          title: 'analysis 2',
          interventionInclusions: [{
            interventionId: intervention1.id,
          }, {
            interventionId: intervention2.id,
          }]
        }];

        var expectedResult = {
          inAnalyses: {
            1: ['analysis 1', 'analysis 2'],
            2: ['analysis 2'],
            3: [],
            4: []
          },
          inInterventions: {
            1: ['1 plus 2', '1 or 2'],
            2: ['1 plus 2', '1 or 2'],
            3: ['1 or 2']
          }
        };

        var result = projectService.buildInterventionUsage(analyses, interventions);

        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildOutcomeUsage', function() {
      it('should build a usage map of the outcomes', function() {
        var outcome1 = {
          id: 1
        };
        var outcome2 = {
          id: 2
        };
        var outcome3 = {
          id: 3
        };
        var outcome4 = {
          id: 4
        };
        var outcomes = [outcome1, outcome2, outcome3, outcome4];
        var analyses = [{
          analysisType: 'Evidence synthesis',
          title: 'nma',
          outcome: {
            id: outcome1.id
          }
        }, {
          analysisType: 'Benefit-risk analysis',
          title: 'ssbr',
          benefitRiskStudyOutcomeInclusions: [{
            outcomeId: outcome1.id
          }, {
            outcomeId: outcome2.id
          }],
          benefitRiskNMAOutcomeInclusions: []
        }, {
          analysisType: 'Benefit-risk analysis',
          title: 'metabr',
          benefitRiskStudyOutcomeInclusions: [],
          benefitRiskNMAOutcomeInclusions: [{
            outcomeId: 3
          }]
        }];

        var expectedResult = {
          1: ['nma', 'ssbr'],
          2: ['ssbr'],
          3: ['metabr'],
          4: []
        };

        var result = projectService.buildOutcomeUsage(analyses, outcomes);

        expect(result).toEqual(expectedResult);
      });
    });

    // var interventions = [simple, fixedOK, fixedMissing, titratedOK, titrateMissing, combination, set];
    describe('addMissingMultiplierInfo', function() {
      it('should do nothing for simple, combination or set interventions', function() {
        var simple = {
          type: 'simple'
        };
        var combination = {
          type: 'combination'
        };
        var set = {
          type: 'class'
        };
        var interventions = [simple, combination, set];

        var result = projectService.addMissingMultiplierInfo(interventions);
        var anyMissing = _.find(result, 'hasMissingMultipliers');

        expect(anyMissing).toBeFalsy();
      });
      it('should check fixed interventions', function() {
        var fixedOK = {
          type: 'fixed',
          constraint: {
            lowerBound: null,
            upperBound: {
              conversionMultiplier: 0.001
            }
          }
        };
        var fixedMissing = {
          type: 'fixed',
          constraint: {
            lowerBound: null,
            upperBound: {}
          }
        };
        var interventions = [fixedOK, fixedMissing];

        var result = projectService.addMissingMultiplierInfo(interventions);

        expect(result[0].hasMissingMultipliers).toBeFalsy();
        expect(result[1].hasMissingMultipliers).toBeTruthy();
      });
      it('should check titrated and BothType interventions', function() {
        var titratedOK = {
          type: 'titrated',
          minConstraint: {
            lowerBound: null,
            upperBound: {
              conversionMultiplier: 0.001
            }
          },
          maxConstraint: {
            lowerBound: {
              conversionMultiplier: 0.01
            },
            upperBound: null
          }

        };
        var titratedMissing = {
          type: 'titrated',
          minConstraint: {
            lowerBound: {
              conversionMultiplier: 0.001
            },
            upperBound: null
          },
          maxConstraint: {
            lowerBound: {
              conversionMultiplier: 0.01
            },
            upperBound: {}
          }
        };
        var bothTypeMissing = _.cloneDeep(titratedMissing);
        bothTypeMissing.type = 'both';
        var interventions = [titratedOK, titratedMissing, bothTypeMissing];

        var result = projectService.addMissingMultiplierInfo(interventions);

        expect(result[0].hasMissingMultipliers).toBeFalsy();
        expect(result[1].hasMissingMultipliers).toBeTruthy();
        expect(result[1].hasMissingMultipliers).toBeTruthy();
      });
    });

  });
});
