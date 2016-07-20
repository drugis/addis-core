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

    function flattenList(rootNode) {
      var list = [];
      if (_.isEmpty(rootNode)) {
        return list;
      }
      var currentNode = rootNode;

      while (true) {
        var node = angular.copy(currentNode.first);
        if (currentNode['@id']) {
          node.blankNodeId = currentNode['@id'];
        }
        list.push(node);
        if (currentNode.rest === rdfListNil || currentNode.rest['@id'] === rdfListNil) {
          return list;
        }
        currentNode = currentNode.rest;
      }
    }

    function unFlattenList(list) {
      return list.reverse().reduce(function(accum, listItem) {
        var newList = {
          first: listItem,
          rest: _.isEmpty(accum) ? rdfListNil : accum
        };
        if (listItem.blankNodeId) {
          newList['@id'] = listItem.blankNodeId;
          delete newList.first.blankNodeId;
        }

        return newList;
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
