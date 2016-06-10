'use strict';
define(['lodash'],
  function(_) {
    var dependencies = [];
    var ProjectService = function() {

      function checkforDuplicateName(itemList, itemName) {
        return _.find(itemList, function(item) {
          return item.name === itemName;
        });
      }

      return {
        checkforDuplicateName: checkforDuplicateName
      };
    };
    return dependencies.concat(ProjectService);
  });
