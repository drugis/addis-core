'use strict';
define(['angular-mocks', './intervention'], function() {
  describe('intervention service', function() {

    var interventionService;
    var bound = {
      type: 'AT_LEAST',
      unitName: 'unit',
      unitPeriod: 'P1D',
      value: '1.2'
    };
    var simpleIntervention = {
      type: 'simple'
    };
    var fixedIntervention = {
      type: 'fixed',
      constraint: {
        upperBound: bound,
        lowerBound: bound
      }
    };
    var titratedIntervention = {
      type: 'titrated',
      minConstraint: {
        upperBound: bound,
        lowerBound: bound
      },
      maxConstraint: {
        upperBound: bound,
        lowerBound: bound
      }
    };
    var bothIntervention = {
      type: 'both',
      minConstraint: {
        upperBound: bound,
        lowerBound: bound
      },
      maxConstraint: {
        upperBound: bound,
        lowerBound: bound
      }
    };

    var interventions = [{
      id: 1,
      name: 'placebo'
    }, {
      id: 2,
      name: 'sertraline'
    }];

    var combinationIntervention = {
      type: 'combination',
      interventionIds: [1, 2]
    };

    var interventionSet = {
      type: 'class',
      interventionIds: [1, 2]
    };

    beforeEach(angular.mock.module('addis.interventions'));

    beforeEach(inject(function(InterventionService) {
      interventionService = InterventionService;
    }));

    describe('constants', function() {
      it('should provide the upper and lower constants', function() {
        expect(interventionService.LOWER_BOUND_OPTIONS).toBeDefined();
        expect(interventionService.UPPER_BOUND_OPTIONS).toBeDefined();
      });
    });

    describe('generateDescriptionLabel', function() {
      it('should generate a label based on the constaints', function() {
        expect(interventionService.generateDescriptionLabel(simpleIntervention)).toEqual('');
        expect(interventionService.generateDescriptionLabel(fixedIntervention)).toEqual(': fixed dose; dose >= 1.2 unit/day AND >= 1.2 unit/day');
        expect(interventionService.generateDescriptionLabel(titratedIntervention)).toEqual(': titrated dose; min dose >= 1.2 unit/day AND >= 1.2 unit/day; max dose >= 1.2 unit/day AND >= 1.2 unit/day');
        expect(interventionService.generateDescriptionLabel(bothIntervention)).toEqual(': min dose >= 1.2 unit/day AND >= 1.2 unit/day; max dose >= 1.2 unit/day AND >= 1.2 unit/day');
        expect(interventionService.generateDescriptionLabel(combinationIntervention, interventions)).toEqual('placebo + sertraline');
        expect(interventionService.generateDescriptionLabel(interventionSet, interventions)).toEqual('placebo OR sertraline');
      });
    });

    describe('cleanUpBounds', function() {
      it('should clean up a fixed dose intervention', function() {
        var fixedDoseCommand = {
          type: 'fixed',
          fixedDoseConstraint: {
            lowerBound: {
              isEnabled: true
            },
            upperBound: {
              bla: 'bla'
            }
          }
        };

        var result = interventionService.cleanUpBounds(fixedDoseCommand);

        var expectedResult = {
          type: 'fixed',
          fixedDoseConstraint: {
            lowerBound: {
              isEnabled: true
            }
          }
        };

        expect(result).toEqual(expectedResult);
      });
      it('should clean up a titrated dose intervention', function() {
        var titratedDoseCommand = {
          type: 'titrated',
          titratedDoseMinConstraint: {
            lowerBound: {
              isEnabled: true
            },
            upperBound: {
              bla: 'bla'
            }
          },
          titratedDoseMaxConstraint: {
            lowerBound: {
              bla: 'bla'
            },
            upperBound: {
              foo: 'bar'
            }
          }
        };

        var result = interventionService.cleanUpBounds(titratedDoseCommand);

        var expectedResult = {
          type: 'titrated',
          titratedDoseMinConstraint: {
            lowerBound: {
              isEnabled: true
            }
          }
        };

        expect(result).toEqual(expectedResult);
      });
      it('should clean up a both-dose intervention', function() {
        var titratedDoseCommand = {
          type: 'both',
          bothDoseTypesMinConstraint: {
            lowerBound: {
              isEnabled: true
            },
            upperBound: {
              bla: 'bla'
            }
          },
          bothDoseTypesMaxConstraint: {
            lowerBound: {
              bla: 'bla'
            },
            upperBound: {
              foo: 'bar'
            }
          }
        };

        var result = interventionService.cleanUpBounds(titratedDoseCommand);

        var expectedResult = {
          type: 'both',
          bothDoseTypesMinConstraint: {
            lowerBound: {
              isEnabled: true
            }
          }
        };

        expect(result).toEqual(expectedResult);
      });
    });
  });
});
