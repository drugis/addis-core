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

    function findNode(id, graph) {
      return _.find(graph, function(node) {
        return node['@id'] === id;
      });
    }

    function findTailNode(list, graph) {
      if (list === {}) {
        return null;
      } else {
        var checkingNode = findNode(list.rest['@id'], graph);
        while (checkingNode['@id'] !== 'nil') {
          checkingNode = findNode(checkingNode.rest['@id']);
        }
        return checkingNode;
      }
    }

    return {
      addItem: addItem
    };
  };
  return dependencies.concat(RdfListService);
});
