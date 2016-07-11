'use strict';
define(['angular', 'angular-mocks', 'util/util'], function() {
  fdescribe('rdfListServiceSpec', function() {

    var rdfListService;
    var listTree = {
      '@id': 'blankUri1',
      'first': {
        '@id': 'instanceUri1',
        label: 'list instance 1'
      },
      'rest': {
        '@id': 'blankUri2',
        'first': {
          '@id': 'instanceUri2',
          label: 'list instance 2'
        },
        'rest': {
          first: {
            '@id': 'instanceUri3',
            label: 'list instance 3'
          },
          rest: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
        }
      }
    };
    var flatList = [{
      '@id': 'instanceUri1',
      blankNodeId: 'blankUri1',
      label: 'list instance 1'
    }, {
      '@id': 'instanceUri2',
      blankNodeId: 'blankUri2',
      label: 'list instance 2'
    }, {
      '@id': 'instanceUri3',
      label: 'list instance 3'
    }];

    beforeEach(function() {
      module('trialverse.util');
    });

    beforeEach(inject(function(RdfListService) {
      rdfListService = RdfListService;
    }));

    describe('flattenList', function() {
      it('should return an empty array for an empty object node', function() {
        expect(rdfListService.flattenList({})).toEqual([]);
      });
      it('should work for a single-item list', function() {
        var singleItemList = {
          '@id': 'blankUri1',
          'first': {
            '@id': 'instanceUri1',
            label: 'list instance 1'
          },
          rest: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
        };
        expect(rdfListService.flattenList(singleItemList)).toEqual([{
          '@id': 'instanceUri1',
          blankNodeId: 'blankUri1',
          label: 'list instance 1'
        }]);
      });
      it('should create a flat array from a root node', function() {
        var result = rdfListService.flattenList(listTree);
        expect(result.length).toBe(3);
        expect(result).toEqual(flatList);
      });
    });

    describe('unflattenList', function() {
      it('should recreate the node tree from the array', function() {
        expect(rdfListService.unFlattenList(flatList)).toEqual(listTree);
      });
    });

    xdescribe('addItem', function() {
      it('should add an item to an empty list', function() {
        var list = [];
        var item = {
          '@id': 1
        };
        var studyNode = {
          '@type': 'ontology:Study'
        };
        var graph = [studyNode];
        var result = rdfListService.addItem(list, item, graph);
        expect(result.size).toBe(2);
        expect(studyNode.has_epochs).not.toBe(null);
      });
    });
  });

});
