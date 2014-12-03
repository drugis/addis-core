'use strict';
define(['angular', 'rdfstore', 'jquery-rdfquery-core', 'jquery-rdfquery-rdfa', 'jquery-rdfquery-rules'],
  function(angular, rdfstore, rdfqueryCore, rdfqueryRdfa, rdfqueryRules) {
    var dependencies = ['$scope', '$stateParams', 'StudyResource', '$location', '$anchorScroll', '$modal', 'RdfstoreService', '$http'];
    var StudyController = function($scope, $stateParams, StudyResource, $location, $anchorScroll, $modal, RdfstoreService, $http) {

      var req = {
        method: 'GET',
        url: 'http://localhost:8090/datasets/dc98f02a-c30b-4877-8b54-acb5885ad04b/studies/02f2456e-1b0f-4095-9e3b-123f0d9ee1a1',
        headers: {
          'Content-Type': 'text/n3'
        }
      };

      $http(req).success(function(data, status, headers, config) {
        //StudyResource.get($stateParams).$promise.then(function(result) {
        // var arms;
        // var study;

        // if (result['@graph']) {

        //   study = _.find(result['@graph'], function(item) {
        //     return item['@type'] === 'http://trials.drugis.org/ontology#Study';
        //   });

        //   arms = _.filter(result['@graph'], function(item) {
        //     return item['@type'] === 'http://trials.drugis.org/ontology#Arm';
        //   });
        // } else {
        //   study = result;
        // }

        // study.arms = arms;
        // $scope.study = study;

        var store;


        var query =
          'prefix ontology: <http://trials.drugis.org/ontology#>' +
          'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
          'prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
          'prefix study: <http://trials.drugis.org/studies/>' +
          'prefix instance: <http://trials.drugis.org/instances/>' +
          'select' +
          ' ?label' +
          ' where {' +
          '    ?armUid' +
          '      rdf:type ontology:Arm ;' +
          '      rdfs:label ?label . ' +
          '}';

        // $.rdf.databank([
        //   $.rdf.triple('_:book1 dc:title "SPARQL Tutorial" .', {
        //     namespaces: { dc: 'http://purl.org/dc/elements/1.1/' }
        //   }),
        //   $.rdf.triple('_:book1  ns:price  42 .', {
        //     namespaces: { ns: 'http://www.example.org/ns/' }
        //   })]);


        // var foo = $.rdf().load(result, {});
        // console.log(foo);


        rdfstore.create(function(store) {
          store.load('text/n3', data, function(success, results) {

            var query =
              'prefix ontology: <http://trials.drugis.org/ontology#>' +
              'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
              'prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
              'prefix study: <http://trials.drugis.org/studies/>' +
              'prefix instance: <http://trials.drugis.org/instances/>' +
              'select' +
              ' ?label' +
              ' where {' +
              '    ?armUid' +
              '      rdf:type ontology:Arm ;' +
              '      rdfs:label ?label . ' +
              '}';

            store.execute(query, function(success, results) {
              if (success) {
                console.log(results);
              } else {
                console.error('query failed!');
              }
            });
          });
        });

        // var data = {} ;
        // data['@context'] = result['@context'];
        // data['@graph'] = result['@graph'];

        // RdfstoreService.load(store,  data['@graph'])
        //   .promise.then(function(store) {
        //     store.graph(function(isSucces, result) {
        //        console.log(isSucces);
        //       console.log(result);
        //     });
        //     store.execute(query, function(isSucces, result) {
        //        console.log(isSucces);
        //       _.each(result, function(r){
        //         console.log(r);
        //       });
        //     });
        //     RdfstoreService.execute(store, query)
        //       .promise.then(function(result) {
        //      //   console.log(result);
        //       });
        //   });
        //  });
      });

      $scope.sideNavClick = function(anchor) {
        var newHash = anchor;
        if ($location.hash() !== newHash) {
          $location.hash(anchor);
        } else {
          $anchorScroll();
        }
      };

      $scope.showArmDialog = function() {
        $modal.open({
          templateUrl: 'app/js/study/view/arm.html',
          scope: $scope
        });
      };
    };

    return dependencies.concat(StudyController);
  });