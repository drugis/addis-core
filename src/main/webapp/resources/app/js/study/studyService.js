'use strict';
define([], function() {
  var dependencies = ['$q', '$filter', 'UUIDService', 'RemoteRdfStoreService'];
  var StudyService = function($q, $filter, UUIDService, RemoteRdfStoreService) {

    var studyPrefix = 'http://trials.drugis.org/studies/';
    var loadDefer = $q.defer();
    var scratchStudyUri,
      modified = false;

    function doModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeUpdate(scratchStudyUri, query).then(function() {
          modified = true;
        });
      })
    }

    function doNonModifyingQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeQuery(scratchStudyUri, query);
      });
    }

    function createEmptyStudy(study) {
      loadDefer = $q.defer();
      return RemoteRdfStoreService.create(studyPrefix)
        .then(function(newGraphUri) {
          scratchStudyUri = newGraphUri;
          var query =
            'PREFIX ontology: <http://trials.drugis.org/ontology#> ' +
            'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ' +
            'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ' +
            'PREFIX study: <http://trials.drugis.org/studies/> ' +
            ' INSERT DATA ' +
            ' { ' +
            '   GRAPH <' + newGraphUri + '> {' +
            '    <' + newGraphUri + '> rdfs:label "' + study.label + '" ; ' +
            '       rdf:type  ontology:Study ; ' +
            '       rdfs:comment   "' + study.comment + '" . ' +
            '   } ' +
            ' }';
          return RemoteRdfStoreService.executeUpdate(newGraphUri, query).then(function() {
            loadDefer.resolve();
          });
        });
    }


    function doSingleResultQuery(query) {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.executeQuery(scratchStudyUri, query).then(function(results) {
          var singleResult = results.data.results.bindings.length === 1 ? results.data.results.bindings[0] : console.error('single result expected');
          return singleResult;
        });
      });
    }

    function queryStudyData() {
      var studyDataQuery =
        'prefix ontology: <http://trials.drugis.org/ontology#>' +
        'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
        'prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
        'prefix study: <http://trials.drugis.org/studies/>' +
        'prefix instance: <http://trials.drugis.org/instances/>' +
        'select' +
        ' ?label ?comment' +
        ' where { GRAPH <' + scratchStudyUri + '> {' +
        '    ?studyUid' +
        '      rdf:type ontology:Study ;' +
        '      rdfs:label ?label ; ' +
        '      rdfs:comment ?comment . ' +
        '}}';
      return loadDefer.promise.then(function() {
        return doSingleResultQuery(studyDataQuery);
      });
    }

    function loadStore(data) {
      return RemoteRdfStoreService.create(studyPrefix).then(function(graphUri) {
        scratchStudyUri = graphUri;
        return RemoteRdfStoreService.load(scratchStudyUri, data).then(function() {
          loadDefer.resolve();
        });
      });
    }

    function getStudyGraph() {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.getGraph(scratchStudyUri);
      });
    }

    function isStudyModified() {
      return modified;
    }

    function studySaved() {
      modified = false;
    }

    function getStudyUUID() {
      return $filter('stripFrontFilter')(scratchStudyUri, studyPrefix);
    }

    function reset() {
      loadDefer = $q.defer();
      modified = false;
    }

    return {
      loadStore: loadStore,
      queryStudyData: queryStudyData,
      createEmptyStudy: createEmptyStudy,
      getStudyGraph: getStudyGraph,
      doModifyingQuery: doModifyingQuery,
      doNonModifyingQuery: doNonModifyingQuery,
      isStudyModified: isStudyModified,
      studySaved: studySaved,
      getStudyUUID: getStudyUUID,
      reset: reset
    };
  };

  return dependencies.concat(StudyService);
});
