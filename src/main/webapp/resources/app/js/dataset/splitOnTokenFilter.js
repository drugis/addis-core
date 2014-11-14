'use strict';
define([], function() {
  var dependencies = [];
  var SplitOnTokenFilter = function() {
    return function(inputString, splitToken) {
      if(!inputString) {
        return inputString;
      }
      // spit by token, comma is used as default split token
      var items = inputString.split(splitToken || ',');

      // trim whitespace
      return _.map(items, function(item) {
        return item.trim();
      });
    };
  };
  return dependencies.concat(SplitOnTokenFilter);
});
