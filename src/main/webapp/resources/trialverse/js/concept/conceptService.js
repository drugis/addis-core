'use strict';
define([], function() {
  var dependencies = ['$q', 'SparqlResource', 'RemoteRdfStoreService', 'UUIDService'];
  var ConceptService = function($q, SparqlResource, RemoteRdfStoreService, UUIDService) {

    var loadDefer = $q.defer();
    var
      conceptsGraphUriBase = 'http://trials.drugis.org/concepts/',
      scratchConceptsGraphUri,
      modified = false;

    var queryConceptsTemplate = SparqlResource.get('queryConcepts.sparql');
    var addConceptTemplate = SparqlResource.get('addConcept.sparql');

    function loadStore(data) {
      console.log('concept loadStore start');
      return RemoteRdfStoreService.create(conceptsGraphUriBase).then(function(graphUri) {
        scratchConceptsGraphUri = graphUri;
        return RemoteRdfStoreService.load(scratchConceptsGraphUri, data).then(function() {
          modified = false;
          loadDefer.resolve();
        });
      });
    }

    function queryItems() {
      return queryConceptsTemplate.then(function(template) {
        return doNonModifyingQuery(template);
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
        .replace(/\$conceptTitle/g, concept.title)
        ;
    }

    return {
      queryItems: queryItems,
      loadStore: loadStore,
      addItem: addItem,
      areConceptsModified: areConceptsModified,
      getGraph: getGraph,
      conceptsSaved: conceptsSaved
    };

  };
  return dependencies.concat(ConceptService);
});
