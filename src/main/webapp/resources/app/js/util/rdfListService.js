'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = [];
  var RdfListService = function() {
    var rdfListNil = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil';

    function addItem(list, item, graph) {
      var newListNode = {
        'first': item['@id'],
        'rest': {
          '@id': 'nil'
        }
      };

      if (!list) {
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

    function buildListItem(listNode, graph) {
      if (!listNode['@list']) { // list with multiple elements
        var node = findNode(listNode.first['@id'], graph);
        node.blankNodeId = listNode['@id'];
        return node;
      } else { // list with one element
        return findNode(listNode['@list'][0], graph);
      }
    }

    function flattenList(rootNode) {
      var list = [];

      var currentNode = rootNode;

      while (true) {
        var node = angular.copy(currentNode.first);
        if (currentNode['@id']) {
          node.blankNodeId = currentNode['@id'];
        }
        list.push(node);
        if (currentNode.rest === rdfListNil) {
          return list;
        }
        currentNode = currentNode.rest;
      }
    }

    function unFlattenList(list) {
      return list.reverse().reduce(function(accum, listItem) {
        accum.first = listItem;
        if (listItem.blankNodeId, idx) {
          accum['@id'] = listItem.blankNodeId;
          delete accum.first.blankNodeId;
        }
        if (idx === list.size - 1) {
          accum.rest = rdfListNil;
        } else {

        }
      }, {});
    }

    return {
      flattenList: flattenList,
      unFlattenList: unFlattenList,
      addItem: addItem
    };
  };
  return dependencies.concat(RdfListService);
});
