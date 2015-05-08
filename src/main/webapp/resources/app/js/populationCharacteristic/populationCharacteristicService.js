'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource', 'MeasurementMomentService', 'OutcomeService'];
    var PopulationCharacteristicService = function($q, StudyService, UUIDService, SparqlResource, MeasurementMomentService, OutcomeService) {

      var addTemplateRaw = SparqlResource.get('addTemplate.sparql');

      var addPopulationCharacteristicQueryRaw = SparqlResource.get('addPopulationCharacteristic.sparql');
      var populationCharacteristicsQuery = SparqlResource.get('queryPopulationCharacteristic.sparql');
      var deletePopulationCharacteristicRaw = SparqlResource.get('deleteVariable.sparql');
      var editPopulationCharacteristicRaw = SparqlResource.get('editVariable.sparql');

      var queryMeasuredAtTemplate = SparqlResource.get('queryMeasuredAt.sparql');

      function queryItems() {
        var items, measuredAtMoments, measurementMoments;

        var queryItemsPromise = populationCharacteristicsQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result){
            items = result;
          });
        });

        var measuredAtQueryPromise = queryMeasuredAtTemplate.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            measuredAtMoments = result;
          });
        });

        var measurementMomentsPromise = MeasurementMomentService.queryItems().then(function(result){
          measurementMoments = result;
        });

        return $q.all([queryItemsPromise, measuredAtQueryPromise, measurementMomentsPromise]).then(function() {
          return _.map(items, function(item) {
            var filtered = _.filter(measuredAtMoments, function(measuredAtMoment) {
              return item.uri === measuredAtMoment.itemUri;
            });

            item.measuredAtMoments = _.map(_.pluck(filtered, 'measurementMoment'), function(measurementMomentUri) {
              return _.find(measurementMoments, function(moment){
                return measurementMomentUri === moment.uri;
              });
            });
            return item;
          });
        });
      }

      function addItem(item) {
        var newItem = angular.copy(item);
        newItem.uuid = UUIDService.generate();
        newItem.uri = 'http://trials.drugis.org/instances/' + newItem.uuid;
        var stringToInsert = buildInsertMeasuredAtBlock(newItem);

        var addItemPromise = addPopulationCharacteristicQueryRaw.then(function(query) {
          var addPopulationCharacteristicQuery =fillInTemplate(query, newItem);
          return StudyService.doModifyingQuery(addPopulationCharacteristicQuery).then(function(){
            return OutcomeService.setOutcomeProperty(newItem);
          });
        });

        var addMeasuredAtPromise = addTemplateRaw.then(function(query) {
          var addMeasuredAtQuery = query.replace('$insertBlock', stringToInsert);
          return StudyService.doModifyingQuery(addMeasuredAtQuery);
        });

        return $q.all([addItemPromise, addMeasuredAtPromise]);
      }

      function deleteItem(item) {
        return deletePopulationCharacteristicRaw.then(function(deleteQueryRaw) {
          return StudyService.doModifyingQuery(deleteQueryRaw.replace(/\$URI/g, item.uri));
        });
      }

      function editItem(item) {
        var newItem = angular.copy(item);
        newItem.measurementMomentBlock = buildInsertMeasuredAtBlock(newItem);
        return editPopulationCharacteristicRaw.then(function(editQueryRaw) {
          var editQuery = fillInTemplate(editQueryRaw, newItem);
          return StudyService.doModifyingQuery(editQuery).then(function(){
            return OutcomeService.setOutcomeProperty(item);
          });
        });
      }

      function buildInsertMeasuredAtBlock(item) {
        return _.reduce(item.measuredAtMoments, function(accumulator, measuredAtMoment){
          return accumulator + ' <' + item.uri + '> ontology:is_measured_at <' + measuredAtMoment.uri + '> .';
        }, '');
      }

      function fillInTemplate(template, item) {
        return template
               .replace(/\$UUID/g, item.uuid)
               .replace('$label', item.label)
               .replace('$measurementType', item.measurementType)
               .replace('$insertMeasurementMomentBlock', item.measurementMomentBlock)
               .replace(/\$URI/g, item.uri)
              ;
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(PopulationCharacteristicService);
  });
