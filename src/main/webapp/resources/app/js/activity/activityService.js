'use strict';
define([],
  function() {
    var dependencies = ['$q', '$filter', 'StudyService', 'SparqlResource', 'UUIDService'];
    var ActivityService = function($q, $filter, StudyService, SparqlResource, UUIDService) {

      var queryActivityTemplate = SparqlResource.get('queryActivity.sparql');
      // var addActivityTemplate = SparqlResource.get('addActivity.sparql');
      // var editActivityTemplate = SparqlResource.get('editActivity.sparql');
      // var deleteActivityTemplate = SparqlResource.get('deleteActivity.sparql');

      function queryItems(studyUuid) {
        return queryActivityTemplate.then(function(template){
          var query = applyToTemplate(template, {studyUuid: studyUuid});
          return StudyService.doNonModifyingQuery(query);
        })
      }

      function addItem(item) {
        // return addItemQuery.then(function(rawQuery) {

        //   var uuid = UUIDService.generate();
        //   var query = rawQuery
        //     .replace('$newItemUuid', uuid)
        //     .replace('$newLabel', item.label)
        //     .replace('$epochUri', item.epoch.uri)
        //     .replace('$anchorMoment', item.relativeToAnchor)
        //     .replace('$timeOffset', item.offset);
        //   return StudyService.doModifyingQuery(query);
        // });
      }

      function editItem(item) {
        // return editItemQuery.then(function(rawQuery) {
        //   var query = rawQuery
        //     .replace(/\$itemUri/g, item.uri)
        //     .replace('$newLabel', item.label)
        //     .replace('$epochUri', item.epoch.uri)
        //     .replace('$anchorMoment', item.relativeToAnchor)
        //     .replace('$timeOffset', item.offset);
        //   return StudyService.doModifyingQuery(query);
        // });
      }

      function deleteItem(item) {
        // return deleteItemQuery.then(function(rawQuery) {
        //   var query = rawQuery
        //     .replace(/\$itemUri/g, item.uri);
        //   return StudyService.doModifyingQuery(query);
        // });

      }

      function applyToTemplate(template, item) {
        return template.replace(/\$studyUuid/g, item.studyUuid);
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        editItem: editItem,
        deleteItem: deleteItem
      };
    };
    return dependencies.concat(ActivityService);
  });
