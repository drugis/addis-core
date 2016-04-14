function ProjectPage(browser) {
  this.browser = browser;
}

var LOCATORS = {
  body: 'body',
  createOutcomeModal: '#show-create-outcome-modal-btn',
  outcomeSelect: '#outcome-select',
  outcomeNameInput: '#outcome-name-input',
  outcomeMotivationTextArea: '#outcome-desc-txt',
  addOutcomeSubmitBtn: '#add-outcome-sumbit-btn',
  createInterventionModal: '#show-create-intervention-modal-btn',
  simpleInterventionSelect: '#simple-intervention-select',
  doseRestrictedInterventionSelect: '#dose-restricted-intervention-select',
  interventionNameInput: '#intervention-name-input',
  interventionMotivationTextArea: '#intervention-desc-txt',
  addInterventionSubmitBtn: '#add-intervention-sumbit-btn',
  createAnalysisModal: '#show-create-analysis-modal-btn',
  analysisTitleInput: '#analysis-title-input',
  analysisTypeSelect: '#analysis-type-select',
  addAnalysisButton: '#add-analysis-btn',
  addSimpleInterventionTab: '#add-simple-intervention-tab',
  addDoseRestrictedInterventionTab: '#add-dose-restricted-intervention-tab',
  addInterventionFixedDoseTypeRadio: '#add-intervention-fixed-dose-type-radio',
  addInterventionFixedConstraintLowerBoundCheckbox: '#fixed-constraint .lower-bound-checkbox',
  addInterventionFixedConstraintUpperBoundCheckbox: '#fixed-constraint .upper-bound-checkbox',
  addInterventionFixedConstraintLowerBoundValue: '#fixed-constraint .lower-bound-value',
  addInterventionFixedConstraintUpperBoundValue: '#fixed-constraint .upper-bound-value'
};

ProjectPage.prototype = {
  waitForPageToLoad: function() {
    this.browser.waitForElementVisible(LOCATORS.body, 50000);
    this.browser.assert.urlContains('projects');
  },
  end: function() {
    this.browser.end();
  },
  addOutcome: function(shortName, desc) {
    this.browser
      .waitForElementVisible(LOCATORS.createOutcomeModal, 15000)
      .click(LOCATORS.createOutcomeModal)
      .waitForElementVisible(LOCATORS.outcomeSelect, 15000)
      .pause(1000)
      .click(LOCATORS.outcomeSelect)
      .pause(300)
      .setValue(LOCATORS.outcomeSelect, shortName)
      .pause(1300)
      .clearValue(LOCATORS.outcomeMotivationTextArea)
      .setValue(LOCATORS.outcomeMotivationTextArea, desc)
      .pause(300)
      .click(LOCATORS.addOutcomeSubmitBtn)
      .pause(300);
  },
  addSimpleIntervention: function(shortName, desc, name) {
    this.browser
      .waitForElementVisible(LOCATORS.createInterventionModal, 15000)
      .click(LOCATORS.createInterventionModal)
      .waitForElementVisible(LOCATORS.simpleInterventionSelect, 15000)
      .pause(1000)
      .setValue(LOCATORS.simpleInterventionSelect, shortName)
      .pause(1300)
      .clearValue(LOCATORS.interventionMotivationTextArea)
      .setValue(LOCATORS.interventionMotivationTextArea, desc)
      .pause(300);
    if (name) {
      this.browser
        .clearValue(LOCATORS.interventionNameInput)
        .setValue(LOCATORS.interventionNameInput, name);
    }
    this.browser.click(LOCATORS.addInterventionSubmitBtn)
      .pause(300);
  },
  addFixedIntervention: function(shortName, desc, lowerValue, upperValue) {
    this.browser
      .waitForElementVisible(LOCATORS.createInterventionModal, 15000)
      .click(LOCATORS.createInterventionModal)
      .waitForElementVisible(LOCATORS.addSimpleInterventionTab, 15000)
      .click(LOCATORS.addDoseRestrictedInterventionTab)
      .pause(300)
      .setValue(LOCATORS.doseRestrictedInterventionSelect, shortName)
      .pause(300)
      .clearValue(LOCATORS.interventionMotivationTextArea)
      .setValue(LOCATORS.interventionMotivationTextArea, desc)
      .pause(300);
    this.browser
      .click(LOCATORS.addInterventionFixedDoseTypeRadio)
      .pause(300)
      .click(LOCATORS.addInterventionFixedConstraintLowerBoundCheckbox)
      .click(LOCATORS.addInterventionFixedConstraintUpperBoundCheckbox)
      .setValue(LOCATORS.addInterventionFixedConstraintLowerBoundValue, lowerValue)
      .setValue(LOCATORS.addInterventionFixedConstraintUpperBoundValue, upperValue)
      .pause(300);
    this.browser
      .click(LOCATORS.addInterventionSubmitBtn)
      .pause(300);
  },
  addAnalysis: function(type, name) {
    this.browser
      .waitForElementVisible(LOCATORS.createAnalysisModal, 15000)
      .click(LOCATORS.createAnalysisModal)
      .waitForElementVisible(LOCATORS.analysisTypeSelect, 15000)
      .setValue(LOCATORS.analysisTitleInput, name)
      .setValue(LOCATORS.analysisTypeSelect, type)
      .pause(300)
      .click(LOCATORS.addAnalysisButton)
      .pause(500);
  }
};

module.exports = ProjectPage;
