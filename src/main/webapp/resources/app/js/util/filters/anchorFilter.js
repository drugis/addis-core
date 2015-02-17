'use strict';
define([], function() {
  var dependencies = ['$filter', 'MeasurementMomentService'];
  var AnchorFilter = function($filter, MeasurementMomentService) {
    return function(measurementMoment) {

      if (!measurementMoment) {
        return undefined;
      } else {
        return MeasurementMomentService.generateLabel(measurementMoment);
      }
    };
  };
  return dependencies.concat(AnchorFilter);
});