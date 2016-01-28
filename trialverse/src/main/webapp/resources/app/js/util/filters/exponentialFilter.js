'use strict';
define([], function() {
  var dependencies = [];
  var ExponentialFilter = function() {
    return function(exponentialStr) {
      return parseFloat(exponentialStr);
    };
  };
  return dependencies.concat(ExponentialFilter);
});
