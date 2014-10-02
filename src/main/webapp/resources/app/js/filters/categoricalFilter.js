'use strict';
define(['moment'], function(moment) {
  var dependencies = [];
  var CategoricalFilter = function() {
    return function(values) {
      var separator = ' / ';
      var mappedVals = _.map(values, function(value) {
        return value.key + '=' + value.value;
      });
      return mappedVals.join(separator);
    };
  };
  return dependencies.concat(CategoricalFilter);
});