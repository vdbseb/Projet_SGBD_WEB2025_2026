describe('Admin guard', () => {

  it('should allow admin access after login', () => {

    cy.visit('http://localhost:4200/admin');

    cy.contains(/connexion administrateur/i)
      .should('be.visible');

    cy.get('[data-cy="login-input"]')
      .type('AG01');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.contains(/administration/i)
      .click();

    cy.contains(/super admin/i)
      .should('be.visible');

  });

});
