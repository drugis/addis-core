'use strict';
define([], function() {
  var dependencies = [];
  var stripFrontFilter = function() {
    return function(inputString, frontString) {
      if(!inputString || !frontString) {
        return inputString;
      }
      if(frontString.length > inputString.length) {
        return inputString;
      }
      if(inputString.indexOf(frontString) !== 0) {
        return inputString;
      }
      return inputString.substr(frontString.length);
    };
  };
  return dependencies.concat(stripFrontFilter);
});
