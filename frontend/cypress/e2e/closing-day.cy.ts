describe('Closing days flow', () => {

  it('should block reservation on a closed day', () => {

    cy.visit('http://localhost:4200');

    cy.contains(/réserve à bruxelles/i)
      .click();

    cy.get('[data-cy="login-input"]')
      .type('G0001');

    cy.get('[data-cy="login-button"]')
      .click();

    cy.contains(/the atomium padel club/i)
      .should('be.visible');

    cy.get('[data-cy="select-court-button"]')
      .first()
      .click();

    // Si ta date fermée est le 01/06/2026
    cy.contains('19')
      .click();

    cy.contains(/le centre est fermé à cette date/i)
      .should('be.visible');

  });

});
