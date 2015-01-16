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
      var duration =  durationString.slice(1);
      if(duration[0] === 'T'){
        duration =  duration.slice(1);
      }
      var periodType = _.find(getPeriodTypeOptions(), function(option){
        return option.code === duration.slice(-1); // get last char
      });
      var numberOfPeriods = parseInt(duration.substring(0, duration.length - 1));

      return {
        numberOfPeriods: numberOfPeriods,
        periodType: periodType
      };
    }

    return {
      getPeriodTypeOptions: getPeriodTypeOptions,
      parseDuration: parseDuration
    }
  }

  return dependencies.concat(DurationService);

});
