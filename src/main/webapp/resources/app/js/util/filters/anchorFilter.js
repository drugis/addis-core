'use strict';
define([], function() {
  var dependencies = ['$filter'];
  var AnchorFilter = function($filter) {
    return function(anchorUri, offset) {

      if (!anchorUri || !offset) {
        return undefined;
      } else {
          var durationPart = $filter('durationFilter')(offset);
          var prepositionPart =  durationPart === 'instantaneous' ? 'at' : 'from';
          var anchorPart =  anchorUri === 'http://trials.drugis.org/ontology#anchorEpochStart' ? 'start' : 'end';
         
          return ' ' + prepositionPart + ' epoch ' + anchorPart;
      }
    };
  };
  return dependencies.concat(AnchorFilter);
});