'use strict';
define([], function() {
  var dependencies = [];

  var SubsetSelectService = function() {
    function addOrRemoveItem(isSelected, item, items) {
      if (!isSelected) {
        return _.without(items, item)
      } else {
        return items.concat(item);
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