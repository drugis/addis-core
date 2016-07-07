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

      if(!list) {
        list = [];
      }

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
      if (list === []) {
        return null;
      } else {
        var checkingNode = findNode(list.rest['@id'], graph);
        while (checkingNode['@id'] !== 'nil') {
          checkingNode = findNode(checkingNode.rest['@id']);
        }
        return checkingNode;
      }
    }

    function flattenList(rootNodeUri, graph) {
      var currentNode = findNode(rootNodeUri, graph);
      var result = [];

      var dataItem;
      if (!currentNode.first['@id']) {
        dataItem = findNode(currentNode.first, graph);
      } else {
        dataItem = findNode(currentNode.first['@id'], graph);
      }
      dataItem.blankNodeUri = rootNodeUri;
      result.push(dataItem);

      var atEnd = false;
      while (!atEnd) {
        if (currentNode.rest['@list']) { // last item
          result.push(findNode(listBlankNode.rest['@list'][0], graph)); // TODO: check how to ensure non-anonymous blank here
          atEnd = true;
        } else {
          currentNode = findNode(currentNode.rest, graph);
          if (!currentNode.first['@id']) {
            dataItem = findNode(currentNode.first, graph);
          } else {
            dataItem = findNode(currentNode.first['@id'], graph);
          }
          dataItem.blankNodeUri = currentNode.rest;
          result.push(dataItem);
        }
      }
      return result;
    }

    return {
      flattenList: flattenList,
      addItem: addItem
    };
  };
  return dependencies.concat(RdfListService);
});
