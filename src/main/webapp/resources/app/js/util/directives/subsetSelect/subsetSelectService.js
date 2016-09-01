'use strict';
define(['lodash'], function(_) {
  var dependencies = [];

  var SubsetSelectService = function() {
    function addOrRemoveItem(isSelected, item, items, comparisonProperty) {
      if (!isSelected) {
       return _.filter(items, function(listItem){
          return listItem[comparisonProperty] !== item[comparisonProperty];
       });
      } else {
        return items.concat(item);
      }
    }

    function createSelectionList(source, target, comparisonProperty) {
      return _.map(source, function(sourceItem) {
        return !!_.find(target, function(targetItem) {
          return targetItem[comparisonProperty] === sourceItem[comparisonProperty];
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
