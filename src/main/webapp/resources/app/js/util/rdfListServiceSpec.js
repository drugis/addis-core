'use strict';
define(['angular', 'angular-mocks', 'util/util'], function() {
  fdescribe('rdfListServiceSpec', function() {

    var rdfListService;

    beforeEach(function(){
      module('trialverse.util');
    });

    beforeEach(inject(function(RdfListService) {
      rdfListService = RdfListService;
    }));

    describe('addItem', function() {
      it('should add an item to an empty list', function() {
        var list = {};
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
