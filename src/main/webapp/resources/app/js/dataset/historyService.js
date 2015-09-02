'use strict';
define([], function() {
  var dependencies = [];
  var HistoryService = function() {

    function addOrderIndex(historyData) {

      // find head
      var previousMap = _.indexBy(historyData, 'previous');
      var indexMap = _.indexBy(historyData, '@id');

      var head = _.find(historyData, function(item) {
        return !previousMap[item['@id']];
      });

      var result = [];
      var current = head;
      var currentIndex = 0;

      while (current && indexMap[current['@id']]) {
        current.idx = currentIndex++;
        result = result.concat(current);
        current = indexMap[current.previous];
      }
      return result;
    }

    function addMergeIndicators(versionNodes, wholeHistory) {
      var indexMap = _.indexBy(wholeHistory, '@id');

      var seen = {};
      return _.map(versionNodes, function(historyItem) {
        if (historyItem.graph_revision) {
          var graphRevisions = [].concat(historyItem.graph_revision);
          var mergeRevision = _.find(graphRevisions, function(graphRevision) {
            var graphRevisionNode = indexMap[graphRevision];
            var revisionNode = indexMap[graphRevisionNode.revision];

            return !seen[graphRevisionNode.revision] && revisionNode.merge_type === 'es:MergeTypeCopyTheirs';
          });
          if (mergeRevision) {
            seen[indexMap[mergeRevision].revision] = indexMap[mergeRevision];
            return _.extend(historyItem, {
              isMergeOperation: true
            });
          }
        }
        return historyItem;
      });
    }

    return {
      addOrderIndex: addOrderIndex,
      addMergeIndicators: addMergeIndicators
    };
  };
  return dependencies.concat(HistoryService);
});
