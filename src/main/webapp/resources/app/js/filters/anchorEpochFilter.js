'use strict';
define([], function() {
  var dependencies = [];
  var AnchorEpochFilter = function() {
    return function(anchorEpoch) {
      if (anchorEpoch) {
        return anchorEpoch.indexOf('anchorEpoch') === 0 ? anchorEpoch.substring('anchorEpoch'.length) : anchorEpoch ;
      }
      return anchorEpoch;
    };
  };
  return dependencies.concat(AnchorEpochFilter);
});