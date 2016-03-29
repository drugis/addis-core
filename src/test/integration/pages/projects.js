function ProjectsPage(browser) {
  this.browser = browser;
}

var LOCATORS = {
  body: 'body'
};

ProjectsPage.prototype = {
  waitForPageToLoad: function() {
    this.browser.waitForElementVisible(LOCATORS.body, 50000);
  },
  end: function() {
    this.browser.end();
  },
  createProject: function(callback) {
    this.browser
      .click('#create-project-btn')
      .assert.elementPresent('table')
      .useXpath() // every selector now must be xpath
    .click('//tr[td="EDARBI"]/td/button')
      .useCss(); // we're back to CSS now
  }
};

module.exports = ProjectsPage;
