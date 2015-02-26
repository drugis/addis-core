'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService', 'CommentService'];
    var ActivityService = function($q, StudyService, SparqlResource, UUIDService, CommentService) {

      // private
      var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';
      var ONTOLOGY = 'http://trials.drugis.org/ontology#';
      var SCREENING_ACTIVITY = ONTOLOGY + 'ScreeningActivity';
      var WASH_OUT_ACTIVITY = ONTOLOGY + 'WashOutActivity';
      var RANDOMIZATION_ACTIVITY = ONTOLOGY + 'RandomizationActivity';
      var DRUG_TREATMENT_ACTIVITY = ONTOLOGY + 'TreatmentActivity';
      var FOLLOW_UP_ACTIVITY = ONTOLOGY + 'FollowUpActivity';
      var OTHER_ACTIVITY = ONTOLOGY + 'StudyActivity';

      var queryActivityTemplate = SparqlResource.get('queryActivity.sparql');
      var addActivityTemplate = SparqlResource.get('addActivity.sparql');
      var editActivityTemplate = SparqlResource.get('editActivity.sparql');
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
            return mapTypeUrisToObjects(activities);
          });
        })
      }

      function mapTypeUrisToObjects(activities) {
        return _.map(activities, function(activity) {
          activity.activityType = ACTIVITY_TYPE_OPTIONS[activity.activityType];
          return activity;
        });
      }

      function addItem(studyUuid, item) {
        var newActivity = angular.copy(item);
        newActivity.activityUri = INSTANCE_PREFIX + UUIDService.generate();
        var addOptionalDescriptionPromise; 
        var addActivityPromise = addActivityTemplate.then(function(template) {
          var query = applyToTemplate(template, studyUuid, newActivity);
          return StudyService.doModifyingQuery(query);
        });

        if(newActivity.activityDescription) {
          addOptionalDescriptionPromise = CommentService.addComment(newActivity.activityUri, item.activityDescription);
        }

        return $q.all([addActivityPromise, addOptionalDescriptionPromise]);
      }

      function editItem(studyUuid, activity) {
        return editActivityTemplate.then(function(template) {
          var query = applyToTemplate(template, studyUuid, activity)
          return StudyService.doModifyingQuery(query).then(function(){
            // no need to use edit as remove is done in the edit activity
            // therefore wait for edit activity to return
            if(activity.activityDescription) {
              return CommentService.addComment(activity.activityUri, activity.activityDescription);
            }
          });
        });
      }

      function deleteItem(studyUuid, item) {
        // return deleteItemQuery.then(function(rawQuery) {
        //   var query = rawQuery
        //     .replace(/\$itemUri/g, item.uri);
        //   return StudyService.doModifyingQuery(query);
        // });

      }

      function applyToTemplate(template, studyUuid, activity) {
        var query = template.replace(/\$studyUuid/g, studyUuid);
        if(activity) {
          query = query
          .replace(/\$activityUri/g, activity.activityUri)
          .replace(/\$label/g, activity.label)
          .replace(/\$comment/g, activity.activityDescription)
          .replace(/\$activityTypeUri/g, activity.activityType.uri);
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
