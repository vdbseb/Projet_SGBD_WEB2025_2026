describe('Réactivation site', () => {
  it('admin global réactive un site', () => {
    cy.visit('http://localhost:4200/admin');

    cy.contains(/connexion administrateur/i)
      .should('be.visible');

    cy.get('[data-cy="login-input"]')
      .type('AG01');

    cy.get('[data-cy="admin-password-input"]')
      .type('admin123');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.visit('http://localhost:4200/admin');

    cy.contains(/super admin/i)
      .should('be.visible');

    cy.get('[title="Sites"] > .block')
      .click();

    cy.contains(/sites/i)
      .should('be.visible');

    cy.get('button')
      .contains(/réactiver/i)
      .should('be.visible')
      .click();

    cy.get('button')
      .contains(/réactiver/i)
      .should('be.visible')
      .click();
  });
});
