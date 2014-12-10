'use strict';
define([],
  function() {
    var dependencies = ['$q', 'UUIDService', 'RdfStoreService'];
    var StudyService = function($q, UUIDService, RdfStoreService) {

      var that = this;

      function doQuery(query) {
        var defer = $q.defer();
        console.log('executing ' + query)
        that.store.execute(query, function(success) {
          if (success) {
            console.log('query success');
            defer.resolve(success);

            that.store.graph(function(success, graph) {
            console.log('ga hoor hier dan ');
              console.log(graph.toNT());
            });
            console.log()
          } else {
            console.error('query failed! ' + query);
            defer.reject();
          }
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
          newStudyStore.execute(query, function(success, result) {
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

      function addArm(arm, studyUUID) {
        var defer = $q.defer();
        var uuid = UUIDService.generate();
        var addArmQuery =
          'PREFIX ontology: <http://trials.drugis.org/ontology#>' +
          'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
          'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
          'PREFIX study: <http://trials.drugis.org/studies/>' +
          'PREFIX instance: <http://trials.drugis.org/instances/>' +
          ' INSERT DATA ' +
          ' { instance:' + uuid + ' rdfs:label "' + arm.label + '" ; ' +
          '                   rdf:type  ontology:Arm ; ' +
          '                   rdfs:comment   "' + arm.comment + '" . ' +
          '   study:' + studyUUID + ' ontology:contains_arm instance:' + uuid + '. ' +
          ' }';

        that.store.execute(addArmQuery, function(success) {
          if (success) {
            console.log('add arm success');
            defer.resolve();
          } else {
            console.error('armsQuery failed!');
            defer.reject();
          }
        });
        return defer.promise;
      }

      function queryStudyData() {
        var defer = $q.defer();
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

        that.store.execute(studyDataQuery, function(success, results) {
          if (success) {
            var studyData = results.length === 1 ? results[0] : console.error('single result expexted');
            defer.resolve(studyData);
          } else {
            console.error('armsQuery failed!');
            defer.reject();
          }
        });
        return defer.promise;
      }

      function queryArmData() {
        var defer = $q.defer();
        var armsQuery =
          ' prefix ontology: <http://trials.drugis.org/ontology#>' +
          ' prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
          ' prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
          ' select' +
          ' ?armURI ?label ?comment ' +
          ' where {' +
          '    ?armURI ' +
          '      rdf:type ontology:Arm ;' +
          '      rdfs:label ?label . ' +
          '     OPTIONAL { ?armURI rdfs:comment ?comment . } ' +
          '}';

        that.store.execute(armsQuery, function(success, results) {
          if (success) {
            defer.resolve(results);
          } else {
            console.error('armsQuery failed!');
          }
        });
        return defer.promise;
      }

      function loadStore(data) {
        var defer = $q.defer();
        rdfstore.create(function(store) {
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
        return defer.promise;
      }

      function exportGraph() {
        var defer = $q.defer();

        that.store.graph(function(success, graph) {
          defer.resolve(graph.toNT());
        });
        return defer.promise;
      }

      return {
        loadStore: loadStore,
        queryStudyData: queryStudyData,
        queryArmData: queryArmData,
        createEmptyStudy: createEmptyStudy,
        addArm: addArm,
        exportGraph: exportGraph,
        doQuery: doQuery
      };
    };

    return dependencies.concat(StudyService);
  });
