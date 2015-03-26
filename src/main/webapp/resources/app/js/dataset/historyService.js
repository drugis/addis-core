'use strict';
define([], function() {
  var dependencies = [];
  var HistoryService = function() {

    function addOrderIndex(historyData) {

      // find head
      var previousMap = _.indexBy(historyData.data, 'previous');
      var indexMap = _.indexBy(historyData.data, '@id');

      var head = _.find(historyData.data, function(item) {
        return !previousMap[item['@id']];
      });

      var result = [];
      var current = head;
      var currentIndex = 0;

      while(current && indexMap[current['@id']]) {
        current.idx = currentIndex++;
        result = result.concat(current);
        current = indexMap[current.previous];
      }
      return result;
    }

    return {
      addOrderIndex: addOrderIndex
    }
  };
  return dependencies.concat(HistoryService);
});
