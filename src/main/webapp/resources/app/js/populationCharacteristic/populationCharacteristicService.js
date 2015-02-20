'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource', 'MeasurementMomentService', 'OutcomeService'];
    var PopulationCharacteristicService = function($q, StudyService, UUIDService, SparqlResource, MeasurementMomentService, OutcomeService) {

      var addTemplateRaw = SparqlResource.get('addTemplate.sparql');

      var addPopulationCharacteristicQueryRaw = SparqlResource.get('addPopulationCharacteristic.sparql');
      var populationCharacteristicsQuery = SparqlResource.get('queryPopulationCharacteristic.sparql');
      var deletePopulationCharacteristicRaw = SparqlResource.get('deletePopulationCharacteristic.sparql');
      var editPopulationCharacteristicRaw = SparqlResource.get('editPopulationCharacteristic.sparql');

      var queryAdverseEventMeasuredAtRaw = SparqlResource.get('queryMeasuredAt.sparql');

      function queryItems(studyUuid) {
        var items, measuredAtMoments, measurementMoments;

        var queryItemsPromise = populationCharacteristicsQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result){
            items = result;
          });
        });

        var measuredAtQueryPromise = queryAdverseEventMeasuredAtRaw.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            measuredAtMoments = result;
          });
        });

        var measurementMomentsPromise = MeasurementMomentService.queryItems(studyUuid).then(function(result){
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
        var newUUid = UUIDService.generate();
        item.uri = 'http://trials.drugis.org/instances/' + newUUid;
        var stringToInsert = buildInsertMeasuredAtBlock(item);

        var addItemPromise = addPopulationCharacteristicQueryRaw.then(function(query) {
          var addPopulationCharacteristicQuery = query
            .replace(/\$UUID/g, newUUid)
            .replace('$label', item.label)
            .replace('$measurementType', item.measurementType);
          return StudyService.doModifyingQuery(addPopulationCharacteristicQuery).then(function(){
            return OutcomeService.setOutcomeProperty(item);
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
        var stringToInsert = buildInsertMeasuredAtBlock(item);

        return editPopulationCharacteristicRaw.then(function(editQueryRaw) {
          var editQuery = editQueryRaw.replace(/\$URI/g, item.uri)
            .replace('$newLabel', item.label)
            .replace('$newMeasurementType', item.measurementType)
            .replace('$insertMeasurementMomentBlock', stringToInsert);
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

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(PopulationCharacteristicService);
  });
