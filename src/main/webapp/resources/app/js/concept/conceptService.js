'use strict';
define([], function() {
  var dependencies = ['$q', 'SparqlResource', 'RemoteRdfStoreService'];
  var ConceptService = function($q, SparqlResource, RemoteRdfStoreService) {

    var loadDefer = $q.defer();
    var 
      scratchConceptUri,
      modified = false;

    var queryConceptsTemplate = SparqlResource.get('queryConcepts.sparql');

    function loadStore(data) {
      console.log('concept loadStore start');
      return RemoteRdfStoreService.create(studyPrefix).then(function(graphUri) {
        scratchStudyUri = graphUri;
        return RemoteRdfStoreService.load(scratchStudyUri, data).then(function() {
          loadDefer.resolve();
        });
      });
    }

    function doModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeUpdate(scratchStudyUri, query).then(function() {
          modified = true;
        });
      });
    }

    function doNonModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeQuery(scratchStudyUri, query);
      });
    }

    function queryItems() {
      return queryConceptsTemplate.then(function(template) {
        var query = fillInTemplate(template);
        return doNonModifyingQuery(query);
      });
    }

    return {
      queryItems: queryItems,
      loadStore: loadStore
    };

  };
  return dependencies.concat(ConceptService);
});