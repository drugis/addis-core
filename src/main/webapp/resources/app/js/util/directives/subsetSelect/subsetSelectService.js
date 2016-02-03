'use strict';
define([], function() {
  var dependencies = [];

  var SubsetSelectService = function() {
    function addOrRemoveItem(isSelected, item, items) {
      if (!isSelected) {
       return _.filter(items, function(listItem){
          return listItem.uri !== item.uri;
       });
      } else {
        return items.concat(item);
      }
    }

    function createSelectionList(source, target) {
      return _.map(source, function(sourceItem) {
        return !!_.find(target, function(targetItem) {
          return targetItem.uri === sourceItem.uri;
        });
      });
    }

    return {
      addOrRemoveItem: addOrRemoveItem,
      createSelectionList: createSelectionList
    };
  };

  return dependencies.concat(SubsetSelectService);
});