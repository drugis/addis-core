'use strict';
define([], function() {
  var dependencies = [];
  var DurationService = function() {

    function getPeriodTypeOptions() {
      return [{
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
    }

    function parseDuration(durationString) {
      if(durationString === undefined) {
        return undefined;
      }
      var duration = durationString.slice(1);
      if (duration[0] === 'T') {
        duration = duration.slice(1);
      }
      var periodType = _.find(getPeriodTypeOptions(), function(option) {
        return option.code === duration.slice(-1); // get last char
      });
      var numberOfPeriods = parseInt(duration.substring(0, duration.length - 1));

      return {
        numberOfPeriods: numberOfPeriods,
        periodType: periodType
      };
    }

    function generateDurationString(duration) {
      var output = 'P';
      output = output + (duration.periodType.isTime ? 'T' : '');
      output = output + duration.numberOfPeriods + duration.periodType.code;
      return output;
    }

    function isPositiveInteger(anything) {
      return /^[1-9]\d*$/.test(anything);
    }

    function isValidDuration(duration) {
      if (duration === 'PT0S') {
        return true;
      }

      var isTime = duration[1] === 'T';
      var firstChar = duration[0];
      var lastChar = duration.slice(-1);
      var rest;

      var optionFound = !!_.find(getPeriodTypeOptions(), function(option) {
        return lastChar === option.code && option.isTime === isTime;
      });

      if (isTime) {
        rest = duration.slice(2, -1);
      } else {
        rest = duration.slice(1, -1);
      }
      return firstChar === 'P' && optionFound && isPositiveInteger(rest);
    }

    function durationStringToMills(durationString) {
      if(durationString[0] === '-'){
        durationString = durationString.slice(1);
      }
      if(durationString === 'P0D' ) {
        durationString = 'PT0S';
      }
      if(!isValidDuration(durationString)) {
        throw "not a valid duration string";
      }
      if (durationString === 'PT0S') {
        return 0;
      }
      var durationObject = parseDuration(durationString);
      var periodTypeCode = durationObject.periodType.code;
      var numberOfPeriods = durationObject.numberOfPeriods;

      if(periodTypeCode === 'H') {
        return numberOfPeriods * 3600000;
      } else if(periodTypeCode === 'D') {
        return numberOfPeriods * 86400000;
      } else if(periodTypeCode === 'W') {
        return numberOfPeriods * 604800000;
      }
    }

    return {
      getPeriodTypeOptions: getPeriodTypeOptions,
      parseDuration: parseDuration,
      generateDurationString: generateDurationString,
      isValidDuration: isValidDuration,
      durationStringToMills: durationStringToMills
    };
  };

  return dependencies.concat(DurationService);

});
