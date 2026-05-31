describe('Désactivation membre', () => {
  it('admin global désactive un membre', () => {
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

    cy.get('[title="Membres"] > .block').click();

    cy.contains(/membres/i).should('be.visible');

    cy.get('button')
      .contains(/suspendre/i)
      .should('be.visible')
      .click();

    cy.get('button')
      .contains(/suspendre/i)
      .should('be.visible')
      .click();
  });
});
