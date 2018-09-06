'use strict';

define(['angular', 'lodash'], function(angular, _) {

  var groupAllocationOptions = _.keyBy([{
    uri: 'ontology:AllocationRandomized',
    label: 'Randomized'
  }, {
    uri: 'ontology:AllocationNonRandomized',
    label: 'Non-Randomized'
  }, {
    uri: 'unknown',
    label: 'Unknown'
  }], 'uri');


  var blindingOptions = _.keyBy([{
    uri: 'ontology:OpenLabel',
    label: 'Open'
  }, {
    uri: 'ontology:SingleBlind',
    label: 'Single blind'
  }, {
    uri: 'ontology:DoubleBlind',
    label: 'Double blind'
  }, {
    uri: 'ontology:TripleBlind',
    label: 'Triple blind'
  }, {
    uri: 'unknown',
    label: 'Unknown'
  }], 'uri');

  var statusOptions = _.keyBy([{
    uri: 'ontology:StatusRecruiting',
    label: 'Recruiting'
  }, {
    uri: 'ontology:StatusEnrolling',
    label: 'Enrolling'
  }, {
    uri: 'ontology:StatusActive',
    label: 'Active'
  }, {
    uri: 'ontology:StatusCompleted',
    label: 'Completed'
  }, {
    uri: 'ontology:StatusSuspended',
    label: 'Suspended'
  }, {
    uri: 'ontology:StatusTerminated',
    label: 'Terminated'
  }, {
    uri: 'ontology:StatusWithdrawn',
    label: 'Withdrawn'
  }, {
    uri: 'ontology:StatusUnknown',
    label: 'Unknown'
  }, {
    uri: 'unknown',
    label: 'Unknown'
  }], 'uri');

  var activityTypeOptions = [{
    label: 'screening',
    uri: 'ontology:ScreeningActivity'
  }, {
    label: 'wash out',
    uri: 'ontology:WashOutActivity'
  }, {
    label: 'randomization',
    uri: 'ontology:RandomizationActivity'
  }, {
    label: 'drug treatment',
    uri: 'ontology:TreatmentActivity'
  }, {
    label: 'follow up',
    uri: 'ontology:FollowUpActivity'
  }, {
    label: 'other',
    uri: 'ontology:StudyActivity'
  }];

  var studyCategorySettings = {
    studyInformation: {
      service: 'StudyInformationService',
      anchorId: 'study-information',
      helpId: 'study-information',
      header: 'Study Information',
      itemName: 'Study Information',
      itemTemplateName: 'studyInformation.html',
      editItemtemplateUrl: '../studyInformation/editStudyInformation.html',
      editItemController: 'EditStudyInformationController'
    },
    populationInformation: {
      service: 'PopulationInformationService',
      anchorId: 'population-information',
      header: 'Population Information',
      helpId: 'population-information',
      itemName: 'Population Information',
      itemTemplateName: 'populationInformation.html',
      editItemtemplateUrl: '../populationInformation/editPopulationInformation.html',
      editItemController: 'EditPopulationInformationController',
    },
    arms: {
      service: 'ArmService',
      anchorId: 'arms',
      helpId: 'arm',
      header: 'Arms',
      addItemController: 'CreateArmController',
      categoryEmptyMessage: 'No arms defined.',
      itemName: 'arm',
      itemTemplateName: 'arm.html',
      addItemtemplateUrl: '../arm/addArm.html',
      editItemtemplateUrl: '../arm/editArm.html',
      editItemController: 'EditArmController',
      repairItemtemplateUrl: '../arm/repairArm.html',
      repairItemController: 'EditArmController'
    },
    groups: {
      service: 'GroupService',
      anchorId: 'groups',
      helpId: 'other-group',
      header: 'Other groups',
      addItemController: 'CreateGroupController',
      categoryEmptyMessage: 'No other groups defined.',
      itemName: 'group',
      itemTemplateName: 'arm.html',
      addItemtemplateUrl: '../group/addGroup.html',
      editItemtemplateUrl: '../group/editGroup.html',
      editItemController: 'EditGroupController',
      repairItemtemplateUrl: '../group/repairGroup.html',
      repairItemController: 'EditGroupController'
    },
    baselineCharacteristics: {
      service: 'PopulationCharacteristicService',
      anchorId: 'baselineCharacteristics',
      helpId: 'baseline-characteristic',
      header: 'Baseline characteristics',
      addItemtemplateUrl: '../variable/addVariable.html',
      addItemController: 'AddVariableController',
      categoryEmptyMessage: 'No baseline characteristics defined.',
      itemName: 'baseline characteristic',
      itemTemplateName: 'variable.html',
      editItemtemplateUrl: '../variable/editVariable.html',
      editItemController: 'EditVariableController',
      repairItemtemplateUrl: '../outcome/repairOutcome.html',
      repairItemController: 'EditOutcomeController'
    },
    outcomes: {
      service: 'EndpointService',
      anchorId: 'outcomes',
      helpId: 'trialverse-outcome',
      header: 'Outcomes',
      categoryEmptyMessage: 'No outcomes defined.',
      itemName: 'outcome',
      itemTemplateName: 'variable.html',
      addItemController: 'AddVariableController',
      addItemtemplateUrl: '../variable/addVariable.html',
      editItemtemplateUrl: '../variable/editVariable.html',
      editItemController: 'EditVariableController',
      repairItemtemplateUrl: '../outcome/repairOutcome.html',
      repairItemController: 'EditOutcomeController'
    },
    adverseEvents: {
      service: 'AdverseEventService',
      anchorId: 'adverseEvents',
      helpId: 'adverse-event',
      header: 'Adverse events',
      addItemController: 'AddVariableController',
      addItemtemplateUrl: '../variable/addVariable.html',
      categoryEmptyMessage: 'No adverse events defined.',
      itemName: 'adverse event',
      itemTemplateName: 'variable.html',
      editItemtemplateUrl: '../variable/editVariable.html',
      editItemController: 'EditVariableController',
      repairItemtemplateUrl: '../outcome/repairOutcome.html',
      repairItemController: 'EditOutcomeController'
    },
    epochs: {
      service: 'EpochService',
      anchorId: 'epochs',
      helpId: 'epoch',
      header: 'Epochs',
      addItemController: 'AddEpochController',
      categoryEmptyMessage: 'No epochs defined.',
      itemName: 'epoch',
      itemTemplateName: 'epoch.html',
      addItemtemplateUrl: '../epoch/addEpoch.html',
      editItemtemplateUrl: '../epoch/editEpoch.html',
      editItemController: 'EditEpochController',
    },
    measurementMoments: {
      service: 'MeasurementMomentService',
      anchorId: 'measurementMoments',
      helpId: 'measurement-moment',
      header: 'Measurement moments',
      addItemController: 'MeasurementMomentController',
      categoryEmptyMessage: 'No measurement moments defined.',
      itemName: 'measurement moment',
      itemTemplateName: 'measurementMoment.html',
      addItemtemplateUrl: '../measurementMoment/editMeasurementMoment.html',
      editItemtemplateUrl: '../measurementMoment/editMeasurementMoment.html',
      editItemController: 'MeasurementMomentController',
      repairItemtemplateUrl: '../measurementMoment/repairMeasurementMoment.html',
      repairItemController: 'MeasurementMomentController'
    },
    activities: {
      service: 'ActivityService',
      anchorId: 'activities',
      helpId: 'activity',
      header: 'Activities',
      addItemController: 'ActivityController',
      categoryEmptyMessage: 'No activities defined.',
      itemName: 'activity',
      itemTemplateName: 'activity.html',
      addItemtemplateUrl: '../activity/editActivity.html',
      editItemtemplateUrl: '../activity/editActivity.html',
      editItemController: 'ActivityController',
    }
  };

  return angular.module('addis.constants', [])
    .constant('GROUP_ALLOCATION_OPTIONS', groupAllocationOptions)
    .constant('BLINDING_OPTIONS', blindingOptions)
    .constant('STATUS_OPTIONS', statusOptions)
    .constant('ACTIVITY_TYPE_OPTIONS', activityTypeOptions)
    .constant('STUDY_CATEGORY_SETTINGS', studyCategorySettings);
});
