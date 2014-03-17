'use strict';
define(['angular', 'underscore'], function () {
  var dependencies = [];
  var Select2UtilService = function () {
    return {
      idsToObjects: function (selectedOutcomeIds, outcomes) {
        return _.map(selectedOutcomeIds, function (outcomeId) {
          return _.find(outcomes, function (outcome) {
            return outcome && outcome.id === parseInt(outcomeId, 10);
          });
        });
      },
      objectsToIds: function (outcomes) {
        return _.map(outcomes, function (outcome) {
          return outcome.id.toString();
        });
      }
    };
  };
  return dependencies.concat(Select2UtilService);
});