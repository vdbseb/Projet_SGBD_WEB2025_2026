describe('Public match flow', () => {

  it('should join a public match successfully', () => {

    cy.visit('http://localhost:4200');

    cy.get('[data-cy="header-login-button"]')
      .click();

    cy.get('[data-cy="login-input"]')
      .type('L0002');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.contains(/matchs publics/i)
      .click();

    cy.contains(/match public/i)
      .should('be.visible');

    cy.get('[data-cy="join-public-match-button"]')
      .contains(/payer 15€ et rejoindre/i)
      .first()
      .click();

    cy.get('mat-dialog-container')
      .contains(/payer 15€/i)
      .click();

    cy.contains(/tu as rejoint le match/i)
      .should('be.visible');

  });

});