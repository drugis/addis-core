'use strict';
define([], function() {
  var dependencies = ['$q', 'SparqlResource', 'RemoteRdfStoreService', 'UUIDService'];
  var ConceptService = function($q, SparqlResource, RemoteRdfStoreService, UUIDService) {

    var
      conceptsGraphUriBase = 'http://trials.drugis.org/concepts/',
      scratchConceptsGraphUri,
      modified = false,
      contextJsonPromise;

    function toFrontEnd(contextItem) {
  //     {
  //   "@id" : "http://trials.drugis.org/concepts/01707e6f-b92c-4ff8-b7c5-61cef7b4c80d",
  //   "@type" : "http://trials.drugis.org/ontology#Variable",
  //   "measurementType" : "http://trials.drugis.org/ontology#dichotomous",
  //   "http://www.w3.org/2000/01/rdf-schema#comment" : "",
  //   "http://www.w3.org/2000/01/rdf-schema#label" : "Weight Loss"
  // }
      return {
        uri: contextItem['@id'],
        type: contextItem['@type'],
        measurementType: contextItem.measurementType,
        comment: contextItem['http://www.w3.org/2000/01/rdf-schema#comment'],
        label: contextItem['http://www.w3.org/2000/01/rdf-schema#label']
      }
    }

    function queryItems() {
      return contextJsonPromise.then(function(json) {
        return _.map(json['@graph'], toFrontEnd)
      });
    }

    function addItem(concept) {
      return addConceptTemplate.then(function(template) {
        var query = fillInConceptTemplate(template, concept);
        return doModifyingQuery(query);
      });
    }

    function areConceptsModified() {
      return modified;
    }

    function conceptsSaved() {
      modified = false;
    }

    function getGraph() {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.getGraph(scratchConceptsGraphUri);
      });
    }

    function doModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeUpdate(scratchConceptsGraphUri, query).then(function() {
          modified = true;
        });
      });
    }

    function doNonModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeQuery(scratchConceptsGraphUri, query);
      });
    }


    function fillInTemplate(template) {
      return template;
    }

    function fillInConceptTemplate(template, concept) {
      return template
        .replace(/\$conceptUri/g, 'http://trials.drugis.org/concepts/' + UUIDService.generate())
        .replace(/\$conceptType/g, concept.type.uri)
        .replace(/\$conceptTitle/g, concept.title);
    }

    function loadJson(jsonPromise) {
      contextJsonPromise = jsonPromise;
      return jsonPromise;
    }

    function getGraphAndContext() {
      return contextJsonPromise.then(function(graphAndContext) {
        return graphAndContext;
      });
    }

    function getJsonGraph() {
      return contextJsonPromise.then(function(graph) {
        return graph['@graph'];
      });
    }

    function saveJsonGraph(newGraph) {
      return contextJsonPromise.then(function(jsonLd) {
        jsonLd['@graph'] = newGraph;
        modified = true;
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      areConceptsModified: areConceptsModified,
      getGraph: getGraph,
      conceptsSaved: conceptsSaved,
      loadJson: loadJson
    };

  };
  return dependencies.concat(ConceptService);
});
