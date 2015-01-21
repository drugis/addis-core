'use strict';
define([],
  function() {
    var dependencies = ['$q', '$filter', 'StudyService', 'SparqlResource', 'UUIDService', 'EpochService'];
    var MeasurementMomentService = function($q, $filter, StudyService, SparqlResource, UUIDService, EpochService) {

      var measurementMomentQuery = SparqlResource.get('queryMeasurementMoment.sparql');
      var addItemQuery = SparqlResource.get('addMeasurementMoment.sparql');

      function queryItems() {
        var measurementMoments, epochs;
        var epochsPromise = EpochService.queryItems().then(function(result){
          epochs = result;
        });

        var measurementsMomentsPromise = measurementMomentQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function (result){
            measurementMoments = result;
          });
        });

        return $q.all([epochsPromise, measurementsMomentsPromise]).then(function(){
          return _.map(measurementMoments, function(measurementMoment){
            measurementMoment.epoch = _.find(epochs, function(epoch){
              return measurementMoment.epochUri.value === epoch.uri.value;
            });
            return measurementMoment;
          });
        })
      }

      function addItem(item) {
        return addItemQuery.then(function(rawQuery) {

          var uuid = UUIDService.generate();
          var query = rawQuery
            .replace('$newItemUuid', uuid)
            .replace('$newLabel', item.label)
            .replace('$epochUri', item.epoch.uri.value)
            .replace('$anchorMoment', item.relativeToAnchor)
            .replace('$timeOffset', item.offset);
          return StudyService.doModifyingQuery(query);
        });
      }

      function generateLabel(measurementMoment) {
        if (!measurementMoment.epoch || !measurementMoment.offset || !measurementMoment.relativeToAnchor) {
          return '';
        }
        var offsetStr = (measurementMoment.offset === 'PT0S') ? 'At' : $filter('durationFilter')(measurementMoment.offset) + ' from';
        var anchorStr = measurementMoment.relativeToAnchor === '<http://trials.drugis.org/ontology#anchorEpochStart>' ? 'start' : 'end';
        return offsetStr + ' ' + anchorStr + ' of ' + measurementMoment.epoch.label.value;
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        generateLabel: generateLabel
      };
    };
    return dependencies.concat(MeasurementMomentService);
  });
