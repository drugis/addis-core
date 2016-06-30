'use strict';
define(['lodash'],
  function(_) {
    var dependencies = [];
    var ProjectService = function() {

      function checkforDuplicateName(itemList, itemToCheck) {
        return _.find(itemList, function(item) {
          return itemToCheck.name === item.name && (
            itemToCheck.id === undefined || itemToCheck.id !== item.id); // name is only duplicate if item is not compared to self
        });
      }

      return {
        checkforDuplicateName: checkforDuplicateName
      };
    };
    return dependencies.concat(ProjectService);
  });
