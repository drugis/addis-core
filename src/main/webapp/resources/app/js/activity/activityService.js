'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService'];
    var ActivityService = function($q, StudyService, UUIDService) {

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
            return _.map(study.has_activity, _.partial(createActivityObject, study, jsonGraph));
          });
        });
      }

      function createActivityObject(study, jsonGraph, activity) {
        var result = {
          activityUri: activity['@id'],
          activityType: ACTIVITY_TYPE_OPTIONS[activity['@type']],
          label: activity.label,
          activityDescription: activity.comment
        };
        if (activity.has_drug_treatment) {
          result.treatments = _.map(activity.has_drug_treatment, _.partial(createTreatmentObject, study, jsonGraph));
        }
        return result;
      }

      function findInstance(graph, uri) {
        return _.find(graph['@graph'], function(node) {
          return node['@id'] === uri;
        });
      }

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
        } else if (result.treatmentDoseType === TITRATED_DOSE_TYPE) {
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

      function createTreatmentJson(treatment) {
        function createDoseValue(value) {
          return [{
            dosingPeriodicity: treatment.dosingPeriodicity,
            unit: treatment.doseUnit.uri,
            value: value
          }]
        }
        var newTreatment = {
          '@id': INSTANCE_PREFIX + UUIDService.generate(),
          '@type': treatment.treatmentDoseType,
          treatment_has_drug: treatment.drug.uri
        };
        if (treatment.treatmentDoseType === TITRATED_DOSE_TYPE) {
          newTreatment.treatment_min_dose = createDoseValue(treatment.minValue);
          newTreatment.treatment_max_dose = createDoseValue(treatment.maxValue);
        } else if (treatment.treatmentDoseType === FIXED_DOSE_TYPE) {
          newTreatment.treatment_dose = createDoseValue(treatment.fixedValue);
        }
        return newTreatment;
      }

      function addItem(item) {
        return StudyService.getJsonGraph().then(function(graph) {
          var newItem = {
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            '@type': item.activityType.uri,
            label: item.label,
            has_activity_application: []
          };
          if (item.activityDescription) {
            newItem.comment = item.activityDescription;
          }
          if (item.treatments && item.treatments.length > 1) {
            newItem.has_drug_treatment = _.map(item.treatments, createTreatmentJson);

            angular.forEach(item.treatments, function(treatment) {
              // add drug to graph if it wasn't there yet
              var drug = _.find(graph['@graph'], function(node) {
                return node['@id'] === treatment.drug.uri;
              });
              if (!drug) {
                graph['@graph'].push({
                  '@id': treatment.drug.uri,
                  '@type': 'ontology:Drug',
                  label: treatment.drug.label
                });
              }
              var unit = _.find(graph['@graph'], function(node) {
                return node['@id'] === treatment.doseUnit.uri;
              });
              if (!unit) {
                graph['@graph'].push({
                  '@id': treatment.doseUnit.uri,
                  '@type': 'ontology:Unit',
                  label: treatment.doseUnit.label
                });
              }
            });
          }

          var study = _.find(graph['@graph'], function(graphNode) {
            return graphNode['@type'] === 'ontology:Study';
          });
          study.has_activity.push(newItem);
          _.remove(graph['@graph'], function(graphNode) {
            return graphNode['@type'] === 'ontology:Study';
          });
          graph['@graph'].push(study);

          return StudyService.saveJsonGraph(graph);
        });
      }

      function editItem(item) {
        return StudyService.getStudy().then(function(study) {
          var toEdit = _.find(study.has_activity, function(activity) {
            return item.activityUri === activity['@id'];
          });
          toEdit['@type'] = item.activityType.uri;
          toEdit.label = item.label;
          toEdit.comment = item.activityDescription;
          return StudyService.save(study);
        });
      }

      function deleteItem(item) {
        return StudyService.getStudy().then(function(study) {
          _.remove(study.has_activity, function(activity) {
            return item.activityUri === activity['@id'];
          });
          return StudyService.save(study);
        });
      }

      function formatDouble(num) {
        if (!isNaN(parseFloat(num)) && isFinite(num)) {
          return parseFloat(num).toExponential().replace('+', '');
        }
        return null;
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
