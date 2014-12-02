'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$stateParams', 'StudyResource', '$location', '$anchorScroll', '$modal', 'RdfstoreService'];
    var StudyController = function($scope, $stateParams, StudyResource, $location, $anchorScroll, $modal, RdfstoreService) {

      StudyResource.get($stateParams).$promise.then(function(result) {
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
          ' ?a ?b ?c ' +
          ' where { ' +
          '    ?a rdf:type ontology:Arm .' +
          '    ?a ?b ?c . ' +
          '}';

        var data = {} ;
        data['@context'] = result['@context'];
        data['@graph'] = result['@graph'];

        RdfstoreService.load(store,  data['@graph'])
          .promise.then(function(store) {
            store.graph(function(isSucces, result) {
               console.log(isSucces);
              console.log(result);
            });
            store.execute(query, function(isSucces, result) {
               console.log(isSucces);
              _.each(result, function(r){
                console.log(r);
              });
            });
            RdfstoreService.execute(store, query)
              .promise.then(function(result) {
             //   console.log(result);
              });
          });
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