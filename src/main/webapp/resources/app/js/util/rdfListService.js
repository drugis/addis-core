'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var RdfListService = function() {

    function addItem(list, item, graph) {
      var newListNode = {
        'first': item['@id'],
        'rest': {
          '@id': 'nil'
        }
      };

      var tailNode = findTailNode(list, graph);
      if (!tailNode) {
        list = newListNode;
      } else {
        tailNode.rest = newListNode;
      }
      return list;
    }

    function findTailNode(list) {
      if (list === {}) {
        return null;
      } else {
        var checkingNode = list.rest
        while (checkingNode['@id' !== 'nil']) {
          checkingNode = 
        }
      } else {
        return findTailNode(list.rest);
      }
    }

    return {
      addItem: addItem
    };
  };
  return dependencies.concat(RdfListService);
});
