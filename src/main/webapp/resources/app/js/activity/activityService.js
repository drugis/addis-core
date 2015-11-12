'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService',
      'CommentService', 'SanitizeService'
    ];
    var ActivityService = function($q, StudyService, SparqlResource, UUIDService,
      CommentService, SanitizeService) {

      // private
      var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';
      var ONTOLOGY = 'ontology:';
      var SCREENING_ACTIVITY = ONTOLOGY + 'ScreeningActivity';
      var WASH_OUT_ACTIVITY = ONTOLOGY + 'WashOutActivity';
      var RANDOMIZATION_ACTIVITY = ONTOLOGY + 'RandomizationActivity';
      var DRUG_TREATMENT_ACTIVITY = ONTOLOGY + 'TreatmentActivity';
      var FOLLOW_UP_ACTIVITY = ONTOLOGY + 'FollowUpActivity';
      var OTHER_ACTIVITY = ONTOLOGY + 'StudyActivity';

      var FIXED_DOSE_TYPE = ONTOLOGY + 'FixedDoseDrugTreatment';
      var TITRATED_DOSE_TYPE = ONTOLOGY + 'TitratedDoseDrugTreatment';

      // public
      var ACTIVITY_TYPE_OPTIONS = {};
      ACTIVITY_TYPE_OPTIONS[SCREENING_ACTIVITY] = {
        label: 'screening',
        uri: SCREENING_ACTIVITY
      };
      ACTIVITY_TYPE_OPTIONS[WASH_OUT_ACTIVITY] = {
        label: 'wash out',
        uri: WASH_OUT_ACTIVITY
      };
      ACTIVITY_TYPE_OPTIONS[RANDOMIZATION_ACTIVITY] = {
        label: 'randomization',
        uri: RANDOMIZATION_ACTIVITY
      };
      ACTIVITY_TYPE_OPTIONS[DRUG_TREATMENT_ACTIVITY] = {
        label: 'drug treatment',
        uri: DRUG_TREATMENT_ACTIVITY
      };
      ACTIVITY_TYPE_OPTIONS[FOLLOW_UP_ACTIVITY] = {
        label: 'follow up',
        uri: FOLLOW_UP_ACTIVITY
      };
      ACTIVITY_TYPE_OPTIONS[OTHER_ACTIVITY] = {
        label: 'other',
        uri: OTHER_ACTIVITY
      };

      function queryItems() {
        return StudyService.getJsonGraph().then(function(jsonGraph) {
          return StudyService.getStudy().then(function(study) {
            var activities = convertTypeUrisToTypeOptions(study.has_activity);
            activities = _.map(activities, function(activity) {
              var result = angular.copy(activity);
              result.activityUri = activity['@id'];
              delete(result['@id']);
              if (activity.has_drug_treatment) {
                result.treatments = _.map(activity.has_drug_treatment, _.partial(createTreatmentObject, study, jsonGraph));
                delete result.has_drug_treatment;
              }
              return result;
            });
            return activities;
          });
        });
      }

      function findInstance(graph, uri) {
        return _.find(graph['@graph'], function(node) {
          return node['@id'] === uri;
        });
      };

      function createTreatmentObject(study, graph, treatment) {
        var result = {
          drug: {
            uri: treatment.treatment_has_drug,
            label: findInstance(graph, treatment.treatment_has_drug).label
          },
          treatmentDoseType: treatment['@type']
        };

        if (result.treatmentDoseType === FIXED_DOSE_TYPE) {
          result.fixedValue = treatment.treatment_dose[0].value;
          result.dosingPeriodicity = treatment.treatment_dose[0].dosingPeriodicity;
          result.doseUnit = {
            uri: treatment.treatment_dose[0].unit,
            label: findInstance(graph, treatment.treatment_dose[0].unit).label
          };
        } else {
          result.minValue = treatment.treatment_min_dose[0].value;
          result.maxValue = treatment.treatment_max_dose[0].value;
          result.dosingPeriodicity = treatment.treatment_min_dose[0].dosingPeriodicity;
          result.doseUnit = {
            uri: treatment.treatment_min_dose[0].unit,
            label: findInstance(graph, treatment.treatment_min_dose[0].unit).label
          };
        }
        return result;
      }


      function convertTypeUrisToTypeOptions(activities) {
        return _.map(activities, function(activity) {
          activity.activityType = ACTIVITY_TYPE_OPTIONS[activity['@type']];
          return activity;
        });
      }

      function addTreatment(activityUri, treatment) {
        StudyService.getStudy().then(function(study) {
          var activity = _.find(study.has_activity, function(activity) {
            return activity['@id'] === activityUri;
          });
        });
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

      function addTreatments(treatments, activityUri) {
        var treatmentPromises = [];
        _.each(treatments, function(treatment) {
          if (!treatment.treatmentUri) {
            treatmentPromises.push(addTreatment(activityUri, treatment));
          }
        });
        return treatmentPromises;
      }

      function addItem(item) {
        var newActivity = angular.copy(item);
        newActivity.activityUri = INSTANCE_PREFIX + UUIDService.generate();
        var addOptionalDescriptionPromise;
        var addActivityPromise = addActivityTemplate.then(function(template) {
          var query = fillInTemplate(template, newActivity);
          return StudyService.doModifyingQuery(query);
        });

        var treatmentPromises = addTreatments(item.treatments, newActivity.activityUri);

        if (newActivity.activityDescription) {
          addOptionalDescriptionPromise = CommentService.addComment(newActivity.activityUri, item.activityDescription);
        }

        return $q.all([addActivityPromise, addOptionalDescriptionPromise].concat(treatmentPromises));
      }

      function editItem(activity) {
        var editActivityPromise = editActivityTemplate.then(function(template) {
          var query = fillInTemplate(template, activity);
          return StudyService.doModifyingQuery(query).then(function() {
            // no need to use edit as remove is dbone in the edit activity
            // therefore wait for edit activity to return
            if (activity.activityDescription) {
              return CommentService.addComment(activity.activityUri, activity.activityDescription);
            }
          });
        });
        var treatmentPromises = addTreatments(activity.treatments, activity.activityUri);
        return $q.all(treatmentPromises.concat(editActivityPromise));
      }

      function deleteItem(activity) {
        return deleteActivityTemplate.then(function(template) {
          var query = fillInTemplate(template, activity);
          return StudyService.doModifyingQuery(query);
        });
      }

      function formatDouble(num) {
        if (!isNaN(parseFloat(num)) && isFinite(num)) {
          return parseFloat(num).toExponential().replace('+', '');
        }
        return null;
      }

      function fillInTreatmentTemplate(template, activityUri, treatment) {
        return template.replace(/\$activityUri/g, activityUri)
          .replace(/\$treatmentUri/g, 'http://trials.drugis.org/instances/' + UUIDService.generate())
          .replace(/\$treatmentUnitUri/g, treatment.doseUnit.uri)
          .replace(/\$treatmentUnitLabel/g, treatment.doseUnit.label)
          .replace(/\$treatmentDrugUri/g, treatment.drug.uri)
          .replace(/\$treatmentDrugLabel/g, treatment.drug.label)
          .replace(/\$treatmentDoseType/g, treatment.treatmentDoseType)
          .replace(/\$treatmentFixedValue/g, formatDouble(treatment.fixedValue))
          .replace(/\$treatmentMinValue/g, formatDouble(treatment.minValue))
          .replace(/\$treatmentMaxValue/g, formatDouble(treatment.maxValue))
          .replace(/\$treatmentDosingPeriodicity/g, treatment.dosingPeriodicity);
      }

      function fillInTemplate(template, activity) {
        return template.replace(/\$activityUri/g, activity.activityUri)
          .replace(/\$label/g, activity.label)
          .replace(/\$comment/g, SanitizeService.sanitizeStringLiteral(activity.activityDescription))
          .replace(/\$activityTypeUri/g, activity.activityType.uri);
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
