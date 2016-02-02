'use strict';
define([], function() {
  var dependencies = ['$q', 'UUIDService'];
  var ConceptService = function($q, UUIDService) {

    var conceptsGraphUriBase = 'http://trials.drugis.org/concepts/';
    var modified = false;
    var conceptJsonPromise;

    var typeOptions = {
      'ontology:Drug': {
        uri: 'ontology:Drug',
        label: 'Drug'
      },
      'ontology:Variable': {
        uri: 'ontology:Variable',
        label: 'Variable'
      }
    };

    function toFrontEnd(conceptItem) {
      var frontEnd = {
        uri: conceptItem['@id'],
        type: typeOptions[conceptItem['@type']],
        label: conceptItem.label
      };
      if (conceptItem.comment) {
        frontEnd.comment = conceptItem.comment;
      }
      return frontEnd;
    }

    function toBackEnd(concept) {
      var backEnd = {
        '@id': 'http://trials.drugis.org/concepts/' + UUIDService.generate(),
        '@type': concept.type.uri,
        label: concept.label
      };
      if (concept.comment) {
        backEnd.comment = concept.comment;
      }
      return backEnd;
    }

    function queryItems() {
      return conceptJsonPromise.then(function(json) {
       // transformConceptJson(json);
        return _.map(json['@graph'], toFrontEnd)
      });
    }

    function addItem(concept) {
      return conceptJsonPromise.then(function(json) {
        modified = true;
        //transformConceptJson(json);
        json['@graph'].push(toBackEnd(concept));
        return json;
      });
    }



    function areConceptsModified() {
      return modified;
    }

    function conceptsSaved() {
      modified = false;
    }

    function loadJson(jsonPromise) {
      conceptJsonPromise = jsonPromise;
      return jsonPromise;
    }

    function getGraphAndContext() {
      return conceptJsonPromise.then(function(graphAndContext) {
        return graphAndContext;
      });
    }

    function getJsonGraph() {
      return conceptJsonPromise.then(function(graph) {
        return graph['@graph'];
      });
    }

    function saveJsonGraph(newGraph) {
      return conceptJsonPromise.then(function(jsonLd) {
        jsonLd['@graph'] = newGraph;
        modified = true;
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      areConceptsModified: areConceptsModified,
      conceptsSaved: conceptsSaved,
      loadJson: loadJson,
      getGraphAndContext: getGraphAndContext
    };

  };
  return dependencies.concat(ConceptService);
});