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

      var FIXED_DOSE_TYPE = 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment';
      var TITRATED_DOSE_TYPE = 'http://trials.drugis.org/ontology#TitratedDoseDrugTreatment';

      var queryActivityTemplate = SparqlResource.get('queryActivity.sparql');
      var queryActivityTreatmentTemplate = SparqlResource.get('queryActivityTreatment.sparql');
      var addActivityTemplate = SparqlResource.get('addActivity.sparql');
      var addTitratedTreatmentTemplate = SparqlResource.get('addTitratedTreatment.sparql');
      var addFixedDoseTreatmentTemplate = SparqlResource.get('addFixedDoseTreatment.sparql');
      var editActivityTemplate = SparqlResource.get('editActivity.sparql');
      var deleteActivityTemplate = SparqlResource.get('deleteActivity.sparql');

      // public
      var ACTIVITY_TYPE_OPTIONS = {};
      ACTIVITY_TYPE_OPTIONS[SCREENING_ACTIVITY] = {label: 'screening', uri: SCREENING_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[WASH_OUT_ACTIVITY] = {label: 'wash out', uri: WASH_OUT_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[RANDOMIZATION_ACTIVITY] = {label: 'randomization', uri: RANDOMIZATION_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[DRUG_TREATMENT_ACTIVITY] = {label: 'drug treatment', uri: DRUG_TREATMENT_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[FOLLOW_UP_ACTIVITY] = {label: 'follow up', uri: FOLLOW_UP_ACTIVITY };
      ACTIVITY_TYPE_OPTIONS[OTHER_ACTIVITY] = {label: 'other', uri: OTHER_ACTIVITY };

      function queryItems(studyUuid) {

        var activities, treatmentsRows;

        var activitiesPromise = queryActivityTemplate.then(function(template){
          var query = fillInTemplate(template, studyUuid);
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            // make object {label, uri} from uri's to use as options in select
            activities = convertTypeUrisToTypeOptions(result);
            return;
          });
        });

        var treatmentsPromise = queryActivityTreatmentTemplate.then(function(template){
            var query = fillInTemplate(template, studyUuid);
            return StudyService.doNonModifyingQuery(query).then(function(result) {
              treatmentsRows = result;
              return;
            });
        });

        return $q.all([activitiesPromise, treatmentsPromise]).then(function(){
            // use a map to avoid double loop
            var activitiesMap = _.indexBy(activities, 'activityUri');

            var treatments = _.map(treatmentsRows, createTreatmentObject);

            _.each(treatments, function(treatment) {
                // make sure the activity has a array of treatments
                if(!activitiesMap[treatment.activityUri].treatments) {
                  activitiesMap[treatment.activityUri].treatments = [];
                }
                // assign each treatment to appropriate activity
                activitiesMap[treatment.activityUri].treatments.push(treatment);
            });
            // return list of activities with treatments added
            return _.values(activitiesMap);
        });
      }

      function createTreatmentObject(treatmentRow) {
        var treatment = {
          activityUri: treatmentRow.activityUri,
          treatmentUri: treatmentRow.treatmentUri,
          doseUnit: {
            uri: treatmentRow.treatmentUnitUri,
            label: treatmentRow.treatmentUnitLabel
          },
          drug: {
            uri: treatmentRow.treatmentDrugUri,
            label: treatmentRow.treatmentDrugLabel
          },
          treatmentDoseType: treatmentRow.treatmentDoseType,
          dosingPeriodicity: treatmentRow.treatmentDosingPeriodicity
        }

        if(treatment.treatmentDoseType === FIXED_DOSE_TYPE) {
          treatment.fixedValue = treatmentRow.treatmentFixedValue;
        } else {
          treatment.minValue = treatmentRow.treatmentMinValue;
          treatment.maxValue =  treatmentRow.treatmentMaxValue;
        }

        return treatment;
      }

      function convertTypeUrisToTypeOptions(activities) {
        return _.map(activities, function(activity) {
          activity.activityType = ACTIVITY_TYPE_OPTIONS[activity.activityType];
          return activity;
        });
      }

      function addTreatment(activityUri, treatment) {
        if (treatment.treatmentDoseType === FIXED_DOSE_TYPE) {
          return addFixedDoseTreatmentTemplate.then(function(template) {
            var query = fillInTreatmentTemplate(template, activityUri, treatment);
            return StudyService.doModifyingQuery(query);
          });
        } else if (treatment.treatmentDoseType === TITRATED_DOSE_TYPE) {
          return addTitratedTreatmentTemplate.then(function(template) {
            var query = fillInTreatmentTemplate(template, activityUri, treatment);
            return StudyService.doModifyingQuery(query);
          });
        }
      }

      function addItem(studyUuid, item) {
        var newActivity = angular.copy(item);
        newActivity.activityUri = INSTANCE_PREFIX + UUIDService.generate();
        var addOptionalDescriptionPromise;
        var addActivityPromise = addActivityTemplate.then(function(template) {
          var query = fillInTemplate(template, studyUuid, newActivity);
          return StudyService.doModifyingQuery(query);
        });

        var treatmentPromises = [];
        _.each(item.treatments, function(treatment) {
          treatmentPromises.push(addTreatment(newActivity.activityUri, treatment));
        });

        if(newActivity.activityDescription) {
          addOptionalDescriptionPromise = CommentService.addComment(newActivity.activityUri, item.activityDescription);
        }

        return $q.all([addActivityPromise, addOptionalDescriptionPromise].concat(treatmentPromises));
      }

      function editItem(studyUuid, activity) {
        return editActivityTemplate.then(function(template) {
          var query = fillInTemplate(template, studyUuid, activity);
          return StudyService.doModifyingQuery(query).then(function(){
            // no need to use edit as remove is dbone in the edit activity
            // therefore wait for edit activity to return
            if(activity.activityDescription) {
              return CommentService.addComment(activity.activityUri, activity.activityDescription);
            }
          });
        });
      }

      function deleteItem(activity, studyUuid) {
        return deleteActivityTemplate.then(function(template) {
          var query = fillInTemplate(template, studyUuid, activity);
          return StudyService.doModifyingQuery(query);
        });
      }

      function fillInTreatmentTemplate(template, activityUri, treatment) {
        return template.replace(/\$activityUri/g, activityUri)
          .replace(/\$treatmentUuid/g, UUIDService.generate())
          .replace(/\$treatmentUnitUri/g, treatment.doseUnit.uri)
          .replace(/\$treatmentUnitLabel/g, treatment.doseUnit.label)
          .replace(/\$treatmentDrugUri/g, treatment.drug.uri)
          .replace(/\$treatmentDrugLabel/g, treatment.drug.label)
          .replace(/\$treatmentDoseType/g, treatment.treatmentDoseType)
          .replace(/\$treatmentFixedValue/g, treatment.fixedValue)
          .replace(/\$treatmentMinValue/g, treatment.minValue)
          .replace(/\$treatmentMaxValue/g, treatment.maxValue)
          .replace(/\$treatmentDosingPeriodicity/g, treatment.dosingPeriodicity)
          ;
      }

      function fillInTemplate(template, studyUuid, activity) {
        var query = template.replace(/\$studyUuid/g, studyUuid);
        if(activity) {
          query = query.replace(/\$activityUri/g, activity.activityUri)
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
