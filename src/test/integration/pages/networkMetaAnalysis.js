function DatasetsPage(browser) {
  this.browser = browser;
}

var LOCATORS = {
  body: 'body',
  createModelBtn: '#add-model-btn',
  selectedOutcome: 'select option[selected="selected"]',
  overlapWarning: '#intervention-overlap-warning'
};

var expextedOverlapText = 'Overlapping interventions detected: please exclude interventions to fix this.';

DatasetsPage.prototype = {
  waitForPageToLoad: function() {
    this.browser.waitForElementVisible(LOCATORS.body, 50000);
  },
  end: function() {
    this.browser.end();
  },
  checkSelectedOutcome: function(name) {
    this.browser
      .waitForElementVisible(LOCATORS.selectedOutcome, 15000)
      .assert.containsText(LOCATORS.selectedOutcome, name);
  },
  checkInterventionOverlap: function() {
    this.browser
      .assert.containsText(LOCATORS.overlapWarning, expextedOverlapText)
      .assert.attributeEquals(LOCATORS.createModelBtn, 'disabled', 'true');

  }
};

module.exports = DatasetsPage;
