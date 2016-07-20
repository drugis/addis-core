'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var CategoricalFilter = function() {
    return function(values) {
      var separator = ' / ';
      var mappedVals = _.map(values, function(value) {
        var key = Object.keys(value)[0];
        return key + '=' + value[key];
      });
      return mappedVals.join(separator);
    };
  };
  return dependencies.concat(CategoricalFilter);
});
