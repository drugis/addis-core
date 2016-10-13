'use strict';
define(['angular', 'angular-mocks'], function() {
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
      interventionIds: [1,2]
    };

    var interventionSet = {
      type: 'class',
      interventionIds: [1,2]
    }

    beforeEach(module('addis.interventions'));

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
  });
});
