'use strict';
define([], function() {
  var dependencies = [];
  var dosingFilter = function() {
    return function(inputString, splitToken) {
      if(!inputString) {
        return 'Fixed';
      }
      return inputString;
    };
  };
  return dependencies.concat(dosingFilter);
});
