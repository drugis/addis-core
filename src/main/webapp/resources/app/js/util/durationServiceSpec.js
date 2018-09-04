'use strict';
define(['angular-mocks', './util'], function() {
  describe('the duration service', function() {
    var durationService;

    beforeEach(angular.mock.module('trialverse.util'));

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
        expect(durationService.getPeriodTypeOptions()).toEqual(periodTypeOptions);  
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

    describe('generateDurationString', function() {
      it('should generate a string from hours', function() {
        var threeHours = {
          numberOfPeriods: 3,
          periodType: {
            code: 'H',
            isTime: true,
            label: 'hour(s)'
          }
        };
        expect(durationService.generateDurationString(threeHours)).toEqual('PT3H');
        var fourHours = threeHours;
        fourHours.numberOfPeriods = 4;
        expect(durationService.generateDurationString(fourHours)).toEqual('PT4H');
      });
      it('should generate a string from days', function() {
        var thirteenDays = {
          numberOfPeriods: 13,
          periodType: {
            code: 'D',
            isTime: false,
            label: 'day(s)'
          }
        };
        expect(durationService.generateDurationString(thirteenDays)).toEqual('P13D');
      });
      it('should generate a string from weeks', function() {
        var fourteenWeeks = {
          numberOfPeriods: 14,
          periodType: {
            code: 'W',
            isTime: false,
            label: 'week(s)'
          }
        };
        expect(durationService.generateDurationString(fourteenWeeks)).toEqual('P14W');
      });
    });

    describe('isValidDuration', function() {
      it('should return true for valid strings', function() {
        expect(durationService.isValidDuration('PT3H')).toBe(true);
        expect(durationService.isValidDuration('PT13H')).toBe(true);
        expect(durationService.isValidDuration('P10D')).toBe(true);
        expect(durationService.isValidDuration('PT0S')).toBe(true);
      });
      it('should return false invalid strings', function() {
        expect(durationService.isValidDuration('Pje moederD')).toBe(false);
        expect(durationService.isValidDuration('PTPiets moederH')).toBe(false);
        expect(durationService.isValidDuration('PT3D')).toBe(false);
        expect(durationService.isValidDuration('P')).toBe(false);
      });
    });

    describe('durationStringToMills', function() {
      it('should transform PT0S to 0', function(){
        expect(durationService.durationStringToMills('PT0S')).toBe(0);
      });
      it('should transform a 3 hours to 10 800 000', function(){
        expect(durationService.durationStringToMills('PT3H')).toBe(10800000);
      });
      it('should transform a 30 days to 2 592 000 000', function(){
        expect(durationService.durationStringToMills('P30D')).toBe(2592000000);
      });
      it('should transform a 300 weeks to 181440000000', function(){
        expect(durationService.durationStringToMills('P300W')).toBe(181440000000);
      });
    });

  });
});
