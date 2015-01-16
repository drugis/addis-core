'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the duration service', function() {
    var durationService;

    beforeEach(module('trialverse.util'));

    beforeEach(inject(function(DurationService) {
      durationService = DurationService;
    }));

    describe('getPeriodTypeOptions', function() {
      it('should return the period type options', function() {
        var periodTypeOptions = [{
          code: 'H',
          isTime: true,
          label: 'hour(s)'
        }, {
          code: 'D',
          isTime: false,
          label: 'day(s)'
        }, {
          code: 'W',
          isTime: false,
          label: 'week(s)'
        }];
        expect(durationService.getPeriodTypeOptions()).toEqual(periodTypeOptions)
      });
    });

    describe('parseDuration', function() {
      it('should parse a duration from a string with hours', function() {
        var threeHours = {
          numberOfPeriods: 3,
          periodType: {
            code: 'H',
            isTime: true,
            label: 'hour(s)'
          }
        };
        expect(durationService.parseDuration('PT3H')).toEqual(threeHours);
        var fourHours = threeHours;
        fourHours.numberOfPeriods = 4;
        expect(durationService.parseDuration('PT4H')).toEqual(fourHours);
      });
      it('should parse a duration from a string with days', function() {
        var threeDays = {
          numberOfPeriods: 3,
          periodType: {
            code: 'D',
            isTime: false,
            label: 'day(s)'
          }
        };
        expect(durationService.parseDuration('P3D')).toEqual(threeDays);
      });
      it('should parse a duration from a string with weeks', function() {
        var threeDays = {
          numberOfPeriods: 3,
          periodType: {
            code: 'W',
            isTime: false,
            label: 'week(s)'
          }
        };
        expect(durationService.parseDuration('P3W')).toEqual(threeDays);
      });
    });

  });
});
