'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService', 'CommentService'];
    var ActivityService = function($q, StudyService, SparqlResource, UUIDService, CommentService) {

      // private
      var ONTOLOGY = 'http://trials.drugis.org/ontology#';
      var SCREENING_ACTIVITY = ONTOLOGY + 'ScreeningActivity';
      var WASH_OUT_ACTIVITY = ONTOLOGY + 'WashOutActivity';
      var RANDOMIZATION_ACTIVITY = ONTOLOGY + 'RandomizationActivity';
      var DRUG_TREATMENT_ACTIVITY = ONTOLOGY + 'TreatmentActivity';
      var FOLLOW_UP_ACTIVITY = ONTOLOGY + 'FollowUpActivity';
      var OTHER_ACTIVITY = ONTOLOGY + 'StudyActivity';

      var queryActivityTemplate = SparqlResource.get('queryActivity.sparql');
      var addActivityTemplate = SparqlResource.get('addActivity.sparql');
      // var editActivityTemplate = SparqlResource.get('editActivity.sparql');
      // var deleteActivityTemplate = SparqlResource.get('deleteActivity.sparql');

      // public
      var ACTIVITY_TYPE_OPTIONS = {};
      ACTIVITY_TYPE_OPTIONS[SCREENING_ACTIVITY] = {label: 'screening', uri: SCREENING_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[WASH_OUT_ACTIVITY] = {label: 'wash out', uri: WASH_OUT_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[RANDOMIZATION_ACTIVITY] = {label: 'randomization', uri: RANDOMIZATION_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[DRUG_TREATMENT_ACTIVITY] = {label: 'drug treatment', uri: DRUG_TREATMENT_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[FOLLOW_UP_ACTIVITY] = {label: 'follow up', uri: FOLLOW_UP_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[OTHER_ACTIVITY] = {label: 'other', uri: OTHER_ACTIVITY };

      function queryItems(studyUuid) {
        return queryActivityTemplate.then(function(template){
          var query = applyToTemplate(template, studyUuid);
          return StudyService.doNonModifyingQuery(query).then(function(activities) {
            // make object {label, uri} from uri's to use as options in select
            var ressult = mapTypeUrisToObjects(activities);
            return ressult;
          });
        })
      }

      function mapTypeUrisToObjects(activities) {
        var result =  _.map(activities, function(activity) {
          activity.activityType = ACTIVITY_TYPE_OPTIONS[activity.activityType];
          return activity;
        });
        return result;
      }

      function addItem(studyUuid, item) {
        var newActivity = angular.copy(item);
        newActivity.uuid = UUIDService.generate();
        var addOptionalDescriptionPromise; 
        var addActivityPromise = addActivityTemplate.then(function(template) {
          var query = applyToTemplate(template, studyUuid, newActivity);
          return StudyService.doModifyingQuery(query);
        });

        if(item.description) {
          addOptionalDescriptionPromise = CommentService.addComment(newActivity.uuid, item.description);
        }

        return $q.all([addActivityPromise, addOptionalDescriptionPromise]);
      }

      function editItem(studyUuid, item) {
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

      function deleteItem(studyUuid, item) {
        // return deleteItemQuery.then(function(rawQuery) {
        //   var query = rawQuery
        //     .replace(/\$itemUri/g, item.uri);
        //   return StudyService.doModifyingQuery(query);
        // });

      }

      function applyToTemplate(template, studyUuid, item) {
        var query = template.replace(/\$studyUuid/g, studyUuid);
        if(item) {
          query = query
          .replace(/\$newActivityUuid/g, item.uuid)
          .replace(/\$label/g, item.label)
          .replace(/\$comment/g, item.description)
          .replace(/\$activityTypeUri/g, item.activityType.uri);
        }
        return query;
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        editItem: editItem,
        deleteItem: deleteItem,
        ACTIVITY_TYPE_OPTIONS: ACTIVITY_TYPE_OPTIONS 
      };
    };
    return dependencies.concat(ActivityService);
  });
