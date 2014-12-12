'use strict';
define([], function() {
    var dependencies = ['$q', 'UUIDService', 'RdfStoreService'];
    var StudyService = function($q, UUIDService, RdfStoreService) {

      var that = this,
        modified = false,
        storeLoadedPromise;

      function doModifyingQuery(query) {
        var promise = doQuery(query);
        promise.then(function() {
          modified = true;
        });
        return promise;
      }

      function doNonModifyingQuery(query) {
        return doQuery(query);
      }

      function doQuery(query) {
        var defer = $q.defer();
        console.log('executing ' + query);
      storeLoadedPromise.then(function() {
          that.store.execute(query, function(success, result) {
            if (success) {
              console.log('study service query result: ' + result);
              defer.resolve(result);
            } else {
              console.error('query failed! ' + query);
              defer.reject();
            }
          });
        });
        return defer.promise;
      }

      function createEmptyStudy(uuid, study) {
        var defer = $q.defer();
        var query =
          'PREFIX ontology: <http://trials.drugis.org/ontology#>' +
          'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
          'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
          'PREFIX study: <http://trials.drugis.org/studies/>' +
          ' INSERT DATA ' +
          ' { study:' + uuid + ' rdfs:label "' + study.label + '" ; ' +
          '                   rdf:type  ontology:Study ; ' +
          '                   rdfs:comment   "' + study.comment + '" . ' +
          ' }';

        RdfStoreService.create(function(newStudyStore) {
          newStudyStore.execute(query, function(success) {
            if (success) {
              console.log('create study success');
              newStudyStore.graph(function(success, graph) {
                defer.resolve(graph.toNT());
              });
            } else {
              console.error('create study failed!');
              defer.reject();
            }
          });
        });
        return defer.promise;
      }


      function doSingleResultQuery(query) {
        return doQuery(query).then(function(results) {
          var singleResult = results.length === 1 ? results[0] : console.error('single result expected');
          return singleResult;
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
          ' where {' +
          '    ?studyUid' +
          '      rdf:type ontology:Study ;' +
          '      rdfs:label ?label ; ' +
          '      rdfs:comment ?comment . ' +
          '}';
        return doSingleResultQuery(studyDataQuery);
      }

      function loadStore(data) {
        var defer = $q.defer();
        RdfStoreService.create(function(store) {
          that.store = store;
          that.store.load('text/n3', data, function(success, results) {
            if (success) {
              defer.resolve(results);
            } else {
              console.error('failed loading store');
              defer.reject();
            }
          });
        });
        storeLoadedPromise = defer.promise;
        return defer.promise;
      }

      function exportGraph() {
        var defer = $q.defer();

        that.store.graph(function(success, graph) {
          defer.resolve(graph.toNT());
        });
        return defer.promise;
      }

      function isStudyModified() {
        return modified;
      }

      function studySaved() {
        modified = false;
      }

      return {
        loadStore: loadStore,
        queryStudyData: queryStudyData,
        createEmptyStudy: createEmptyStudy,
        exportGraph: exportGraph,
        doModifyingQuery: doModifyingQuery,
        doNonModifyingQuery: doNonModifyingQuery,
        isStudyModified: isStudyModified,
        studySaved: studySaved
      };
    };

    return dependencies.concat(StudyService);
  });
