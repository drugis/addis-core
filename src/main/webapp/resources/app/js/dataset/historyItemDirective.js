'use strict';
define([], function() {
  var depencencies = [];
  var HistoryItemDirective = function() {
    return {
      restrict: 'E',
      templateUrl: 'app/js/dataset/historyItemDirective.html',
      scope: {
        item: '='
      }
    }
  };
  return depencencies.concat(HistoryItemDirective);
});
