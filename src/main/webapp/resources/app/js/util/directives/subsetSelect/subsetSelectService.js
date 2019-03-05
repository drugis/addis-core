'use strict';
define(['lodash'], function(_) {
  var dependencies = [];

  var SubsetSelectService = function() {
    function addOrRemoveItem(isSelected, item, items, equals) {
      if (!isSelected) {
       return _.filter(items, function(listItem){
          return !equals(listItem, item);
       });
      } else {
        return items.concat(item);
      }
    }

    function createSelectionList(source, target, equals) {
      return _.map(source, function(sourceItem) {
        return _.some(target, function(targetItem) {
          return equals(targetItem, sourceItem);
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
