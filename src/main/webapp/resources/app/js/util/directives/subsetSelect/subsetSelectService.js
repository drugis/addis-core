'use strict';
define([], function() {
  var dependencies = [];

  var SubsetSelectService = function() {
    function addOrRemoveItem(newValue, oldValue, items) {
      if (!newValue) {
        return _.without(items, oldValue)
      } else {
        return items.concat(newValue);
      }
    }

    function createSelectionList(source, target) {
      return _.map(source, function(sourceItem) {
        return _.contains(target, sourceItem) ? sourceItem : false;
      });
    }

    return {
      addOrRemoveItem: addOrRemoveItem,
      createSelectionList: createSelectionList
    };
  }

  return dependencies.concat(SubsetSelectService);
});